#include <algorithm>
#include <array>
#include <cassert>
#include <complex>
#include <cstddef>
#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <ios>
#include <iostream>
#include <map>
#include <optional>
#include <ostream>
#include <queue>
#include <set>
#include <stack>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

using Direction = std::complex<int64_t>;
using Location = std::complex<int64_t>;

struct LocationUtil final {
  bool operator()(const Location &l1, const Location &l2) const noexcept {
    if (l1.real() != l2.real())
      return l1.real() < l2.real();
    if (l1.imag() != l2.imag())
      return l1.imag() < l2.imag();
    return false;
  }
  size_t operator()(const Location &l1) const noexcept {
    return l1.real() * 37 + l1.imag() * 23;
  }
};

template <typename T>
std::ostream &operator<<(std::ostream &out, const std::vector<T> &data) {
  out << '[';
  bool isFirst = true;
  for (auto &item : data) {
    if (isFirst) {
      isFirst = false;
    } else {
      std::cout << ", ";
    }
    std::cout << item;
  }
  return out << ']';
}

constexpr std::array<Direction, 4> dirs = {
    Direction(0, 1),  // R
    Direction(1, 0),  // D
    Direction(0, -1), // L
    Direction(-1, 0), // U
};

struct Command {
  uint8_t dir;
  uint64_t amount;

  Direction getDir() const { return dirs[dir]; }
};

std::ostream &operator<<(std::ostream &out, const Command &comm) {
  return out << '(' << (signed)comm.dir << ',' << comm.amount << ')';
}

template <bool swapped> class LavaContainer {
  std::ifstream input;

protected:
  LavaContainer(const std::string &filename) : input(filename) {}

  uint8_t prevMode = -1;
  std::optional<Command> nextCommand() {
    uint8_t name, skip;
    uint32_t amount;
    uint64_t color;
    input >> name >> amount >> skip >> skip >> std::hex >> color >> std::dec >>
        skip;

    if (!input) {
      return std::nullopt;
    }

    uint8_t mode;
    if (swapped) {
      switch (name) {
      case 'R':
        mode = 0;
        break;
      case 'D':
        mode = 1;
        break;
      case 'L':
        mode = 2;
        break;
      case 'U':
        mode = 3;
        break;
      default:
        exit(2);
      };
    } else {
      mode = color % 16;
      amount = color / 16;
    }

    return std::optional(Command{mode, amount});
  }
};

template <bool swapped> class LavaPool final : private LavaContainer<swapped> {
  std::unordered_set<Location, LocationUtil> visited;

public:
  LavaPool(const std::string &filename) : LavaContainer<swapped>(filename) {}

private:
  void walkAround() {
    Location loc = {0, 0};
    visited.emplace(loc);

    std::optional<Command> commOpt;
    while ((commOpt = LavaContainer<swapped>::nextCommand())) {
      const Command comm = commOpt.value();
      const Direction dir = comm.getDir();

      for (size_t i = 0; i < comm.amount; ++i) {
        loc += dir;
        visited.emplace(loc);
      }
    }
  }

  void fill() {
    std::queue<Location> queue;

    const auto insert = [this, &queue](const Location &&newLoc) {
      if (!visited.count(newLoc)) {
        queue.emplace(newLoc);
        visited.emplace(std::move(newLoc));
      }
    };

    insert(Location(1, 1));

    while (!queue.empty()) {
      const Location loc = queue.front();
      queue.pop();

      insert(std::move(loc + Direction(1, 0)));
      insert(std::move(loc + Direction(-1, 0)));
      insert(std::move(loc + Direction(0, 1)));
      insert(std::move(loc + Direction(0, -1)));
    }
  }

public:
  size_t run() {
    walkAround();
    fill();
    return visited.size();
  }
};

template <bool swapped> class LavaSea final : private LavaContainer<swapped> {

public:
  LavaSea(const std::string &filename) : LavaContainer<swapped>(filename) {}

private:
  static size_t pushMerging(std::vector<Command> &stack, const Command &&item) {
    if (stack.empty()) {
      stack.emplace_back(std::move(item));
      return 0;
    }

    Command &back = stack.back();
    if ((back.dir - item.dir) % 2 != 0) {
      stack.emplace_back(std::move(item));
      return 0;
    }

    if (back.dir == item.dir) {
      back.amount += item.amount;
      return 0;
    }

    if (back.amount >= item.amount) {
      back.amount -= item.amount;
      return item.amount;
    } else {
      const size_t oldAmount = back.amount;
      back.amount = item.amount - back.amount;
      back.dir = (back.dir + 2) % 4;
      return oldAmount;
    }
  }
  static size_t findEnclose(std::vector<Command> &stack, const bool clk) {
    const uint8_t diff = clk ? 3 : 1;
    size_t area = 0;

    while (stack.size() >= 3) {
      const size_t size = stack.size();
      const uint8_t start = stack[size - 1].dir;

      if (static_cast<uint8_t>(stack[size - 2].dir - start) % 4 != diff ||
          static_cast<uint8_t>(stack[size - 3].dir - start) % 4 !=
              (2 * diff) % 4) {
        break;
      }

      Command l1 = std::move(stack.back());
      stack.pop_back();
      Command l2 = std::move(stack.back());
      stack.pop_back();
      Command l3 = std::move(stack.back());
      stack.pop_back();

      if (l1.amount == l3.amount) {
        if (clk) {
          area += l1.amount * (l2.amount + 1);
        } else {
          area -= l1.amount * (l2.amount - 1);
        }

        area += pushMerging(stack, std::move(l2));
      } else if (l1.amount > l3.amount) {
        if (clk) {
          area += l3.amount * (l2.amount + 1);
        } else {
          area -= l3.amount * (l2.amount - 1);
        }

        l1.amount -= l3.amount;

        area += pushMerging(stack, std::move(l2));
        area += pushMerging(stack, std::move(l1));
      } else {
        if (clk) {
          area += l1.amount * (l2.amount + 1);
        } else {
          area -= l1.amount * (l2.amount - 1);
        }

        l3.amount -= l1.amount;

        area += pushMerging(stack, std::move(l3));
        area += pushMerging(stack, std::move(l2));
      }
    }

    return area;
  }

public:
  size_t run() {
    std::vector<Command> stack;
    size_t area = 0;

    std::optional<Command> commOpt;
    while ((commOpt = LavaContainer<swapped>::nextCommand())) {
      Command comm = std::move(commOpt.value());

      // std::cout << "\nCommand:  " << comm << std::endl;
      area += pushMerging(stack, std::move(comm));
      area += findEnclose(stack, true);
      area += findEnclose(stack, false);
      // std::cout << "Progress: " << area << " " << stack << std::endl;
    }
    area += 1;

    // std::cout << "Stack sizes: " << stackClk.size() << "\n";
    // std::cout << "Area:        " << areaClk << std::endl;

    return area;
  }
};

template <bool useSea> size_t part01(const std::string &filename) {
  if constexpr (!useSea) {
    return LavaPool<true>(filename).run();
  } else {
    return LavaSea<true>(filename).run();
  }
}
size_t part02(const std::string &filename) {
  return LavaSea<false>(filename).run();
}

int main() {
  std::string fileTest = "input_test.txt";
  std::string fileProd = "input_prod.txt";

  assert(part01<false>(fileTest) == 62);
  assert(part01<false>(fileProd) == 52055);

  assert(part01<true>(fileTest) == 62);
  const auto part01Res = part01<true>(fileProd);
  std::cout << "Part 01: " << part01Res << std::endl;
  assert(part01Res == 52055);

  assert(part02(fileTest) == 952408144115);
  const auto part02Res = part02(fileProd);
  std::cout << "Part 02: " << part02Res << std::endl;
  assert(part02Res == 67622758357096);
}
