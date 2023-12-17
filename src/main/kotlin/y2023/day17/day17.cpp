#include <algorithm>
#include <array>
#include <bitset>
#include <cassert>
#include <cmath>
#include <complex>
#include <cstdint>
#include <cstdio>
#include <fstream>
#include <iostream>
#include <iterator>
#include <memory>
#include <ostream>
#include <queue>
#include <string>
#include <string_view>
#include <utility>
#include <vector>

using Location = std::complex<int16_t>;
using Direction = std::complex<int16_t>;

class Map final {
private:
  std::unique_ptr<const uint8_t[]> data;
  size_t width;

  Map(std::unique_ptr<const uint8_t[]> &&data, size_t width)
      : data(std::move(data)), width(width) {}

public:
  uint8_t at(const Location &point) const {
    return data.get()[point.real() * width + point.imag()] - '0';
  }

  uint8_t size() const noexcept { return width; }

  void printMap(std::ostream &out = std::cout) const {
    for (size_t i = 0; i < width; ++i) {
      for (size_t j = 0; j < width; ++j) {
        out << at(Location(i, j));
      }
      out << "\n";
    }
  }

  static Map fromFile(const std::string &filename) {
    auto input = std::ifstream(filename);

    std::string buff;
    std::getline(input, buff);
    input >> std::ws;
    size_t width = buff.size();

    auto data = std::make_unique<uint8_t[]>(width * width);

    std::copy(buff.begin(), buff.end(), data.get());

    for (size_t i = 1; i < width; ++i) {
      input.read(reinterpret_cast<char *>(data.get() + i * width), width);
      input >> std::ws;
    }

    return Map(std::move(data), width);
  }
};

struct Position final {
  uint16_t dist;
  uint16_t steps;
  Direction dir;
  Location loc;

  bool operator<(const Position &other) const noexcept {
    if (dist != other.dist)
      return dist < other.dist;
    /*if (steps != other.steps)*/ return steps < other.steps;
  }

  bool operator>(const Position &other) const noexcept {
    if (dist != other.dist)
      return dist > other.dist;
    /*if (steps != other.steps)*/ return steps > other.steps;
  }
};

std::ostream &operator<<(std::ostream &out, const Position &pos) {
  return out << "{" << pos.dist << "," << pos.steps << ",pos:" << pos.loc
             << ",dir:" << pos.dir << "}";
}

template <bool doBacktracking = false> class Dijkstra final {
  const Map &map;
  const size_t width;
  const size_t area = width * width;
  // wanted to use a bit set, but it was not convenient in the end
  std::vector<bool> data;
  std::vector<Position> backtrack;

public:
  Dijkstra(const Map &map)
      : map(map), width(map.size()), data(std::vector<bool>(4 * 3 * area)),
        backtrack(std::vector<Position>(doBacktracking ? 4 * 3 * area : 0)) {}

private:
  size_t hash(const Position &pos) const noexcept {
    uint8_t dirHashReal = static_cast<uint8_t>(pos.dir.real());
    uint8_t dirHashImag = static_cast<uint8_t>(pos.dir.imag());
    const uint8_t hashDir =
        ((dirHashReal | dirHashImag) & 0b10) | (dirHashImag & 0b01);

    const size_t hash = hashDir * 3 * area + pos.steps * area +
                        pos.loc.imag() * width + pos.loc.real();
    return hash;
  }

  void visit(const Position &pos) { data[hash(pos)] = true; }

  bool at(const Position &pos) const { return data[hash(pos)]; }

  std::priority_queue<Position, std::vector<Position>, std::greater<Position>>
      queue;

  void insertStart(Position &&pos) {
    visit(pos);
    if (doBacktracking)
      backtrack[hash(pos)] = pos;
    queue.emplace(std::move(pos));
  }

  void insert(const Position &pos, const Direction &dir,
              const uint16_t newSteps) {
    const Location newLoc = pos.loc + dir;

    if (newSteps >= 3 || static_cast<uint16_t>(newLoc.real()) >= width ||
        static_cast<uint16_t>(newLoc.imag()) >= width) {
      return;
    }

    Position newPos = {static_cast<uint16_t>(pos.dist + map.at(newLoc)),
                       newSteps, dir, newLoc};
    if (at(newPos)) {
      return;
    }

    visit(newPos);
    if (doBacktracking)
      backtrack[hash(newPos)] = pos;
    queue.emplace(std::move(newPos));
  }

  void process(const Position &&pos) {
    insert(pos, pos.dir, pos.steps + 1);
    insert(pos, pos.dir * Direction(0, 1), 0);
    insert(pos, pos.dir * Direction(0, -1), 0);
  }

  void backtrackPos(Position pos) const {
    std::cout << "Backtracking" << std::endl;
    while (true) {
      std::cout << pos << std::endl;
      if (pos.loc == Location(0, 0)) {
        break;
      }
      pos = backtrack[hash(pos)];
    }
  }

public:
  uint16_t run(const Location pos) {

    insertStart({0, 0, Direction(1, 0), Location(0, 0)});
    insertStart({0, 0, Direction(0, 1), Location(0, 0)});

    const Location target = Location(width - 1, width - 1);

    while (!queue.empty()) {
      const auto item = std::move(queue.top());
      queue.pop();

      if (item.loc == target) {
        if (doBacktracking)
          backtrackPos(item);
        return item.dist;
      }

      process(std::move(item));
    }

    return -1;
  }
};

size_t part01(const std::string &filename) {
  const auto map = Map::fromFile(filename);

  auto dijkstra = Dijkstra(map);
  return dijkstra.run(Location(0, 0));
}

size_t part02(const std::string &filename) { return 0; }

int main() {
  std::string fileTest = "input_test.txt";
  std::string fileProd = "input_prod.txt";

  assert(part01(fileTest) == 102);
  std::cout << "Part 01: " << part01(fileProd) << std::endl;
  assert(part02(fileTest) == 0);
  std::cout << "Part 02: " << part02(fileProd) << std::endl;
}
