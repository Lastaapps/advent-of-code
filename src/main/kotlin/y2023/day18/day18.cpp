#include <algorithm>
#include <cassert>
#include <complex>
#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <ios>
#include <iostream>
#include <optional>
#include <ostream>
#include <queue>
#include <string>
#include <unordered_map>
#include <unordered_set>

using Direction = std::complex<int16_t>;
using Location = std::complex<int16_t>;

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

struct Command {
  uint8_t dir;
  uint8_t amount;
  uint32_t color;

  Direction getDir() const {
    switch (dir) {
    case 'U':
      return Direction(-1, 0);
    case 'D':
      return Direction(1, 0);
    case 'L':
      return Direction(0, -1);
    case 'R':
      return Direction(0, 1);
    }
    exit(1);
  }
};

class LavaPool final {
  std::ifstream input;
  std::unordered_set<Location, LocationUtil> visited;

public:
  LavaPool(const std::string &filename) : input(filename) {}

private:
  std::optional<Command> nextCommand() {
    char name, skip;
    int amount;
    int color;
    input >> name >> amount >> skip >> skip >> std::hex >> color >> std::dec >> skip;
    if (!input) {
      return std::nullopt;
    }
    return std::optional(Command{static_cast<uint8_t>(name),
                                 static_cast<uint8_t>(amount),
                                 static_cast<uint32_t>(color)});
  }

  void walkAround() {
    Location loc = {0, 0};
    visited.emplace(loc);

    std::optional<Command> commOpt;
    while ((commOpt = nextCommand())) {
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

size_t part01(const std::string &filename) { 
    return LavaPool(filename).run();
}
size_t part02(const std::string &filename) { return 0; }

int main() {
  std::string fileTest = "input_test.txt";
  std::string fileProd = "input_prod.txt";

  assert(part01(fileTest) == 62);
  const auto part01Res = part01(fileProd);
  std::cout << "Part 01: " << part01Res << std::endl;
  assert(part01Res == 52055);

  assert(part02(fileTest) == 0);
  const auto part02Res = part02(fileProd);
  std::cout << "Part 02: " << part02Res << std::endl;
  assert(part02Res == 0);
}
