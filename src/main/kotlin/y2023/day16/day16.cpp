#include <algorithm>
#include <array>
#include <bitset>
#include <cassert>
#include <cmath>
#include <cstdint>
#include <cstdio>
#include <fstream>
#include <iostream>
#include <iterator>
#include <memory>
#include <string>
#include <string_view>
#include <utility>
#include <vector>

class Map {
private:
  std::unique_ptr<const uint8_t[]> data;
  size_t width;

  Map(std::unique_ptr<const uint8_t[]> &&data, size_t width)
      : data(std::move(data)), width(width) {}

public:
  uint8_t at(size_t x, size_t y) const { return data.get()[x * width + y]; }

  uint8_t size() const noexcept { return width; }

  void printMap(std::ostream &out = std::cout) const {
    for (size_t i = 0; i < width; ++i) {
      for (size_t j = 0; j < width; ++j) {
        out << at(i, j);
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

enum class Direction {
  TOP = 1 << 0,
  BOTTOM = 1 << 1,
  LEFT = 1 << 2,
  RIGHT = 1 << 3,
};

class Dfs {
  const Map &map;
  const size_t width;
  // wanted to use a bit set, but it was not convenient in the end
  std::vector<uint8_t> data;

public:
  Dfs(const Map &map)
      : map(map), width(map.size()),
        data(std::vector<uint8_t>(map.size() * map.size())) {}

private:
  uint8_t &at(const size_t x, const size_t y) { return data[x * width + y]; }
  uint8_t const &at(const size_t x, const size_t y) const {
    return data[x * width + y];
  }

  static Direction tranformDir(const Direction dir, const uint8_t field) {
    switch (field) {
    case '.':
      return dir;
    case '\\':
      switch (dir) {
      case Direction::TOP:
        return Direction::LEFT;
      case Direction::BOTTOM:
        return Direction::RIGHT;
      case Direction::LEFT:
        return Direction::TOP;
      case Direction::RIGHT:
        return Direction::BOTTOM;
      };
    case '/':
      switch (dir) {
      case Direction::TOP:
        return Direction::RIGHT;
      case Direction::BOTTOM:
        return Direction::LEFT;
      case Direction::LEFT:
        return Direction::BOTTOM;
      case Direction::RIGHT:
        return Direction::TOP;
      };
    case '|':
      switch (dir) {
      case Direction::TOP:
      case Direction::BOTTOM:
        return dir;
      case Direction::LEFT:
      case Direction::RIGHT:
        return static_cast<Direction>(static_cast<uint8_t>(Direction::TOP) |
                                      static_cast<uint8_t>(Direction::BOTTOM));
      };
    case '-':
      switch (dir) {
      case Direction::LEFT:
      case Direction::RIGHT:
        return dir;
      case Direction::TOP:
      case Direction::BOTTOM:
        return static_cast<Direction>(static_cast<uint8_t>(Direction::LEFT) |
                                      static_cast<uint8_t>(Direction::RIGHT));
      };
    };

    exit(1);
  }

  void dfs(const size_t x, const size_t y, const Direction dir) {
    if (x >= width || y >= width || at(x, y) & static_cast<uint8_t>(dir)) {
      return;
    }
    at(x, y) |= static_cast<uint8_t>(dir);

    const auto field = map.at(x, y);
    const auto newDirs = tranformDir(dir, field);

    if (static_cast<uint8_t>(newDirs) & static_cast<uint8_t>(Direction::TOP)) {
      dfs(x - 1, y, Direction::TOP);
    }
    if (static_cast<uint8_t>(newDirs) &
        static_cast<uint8_t>(Direction::BOTTOM)) {
      dfs(x + 1, y, Direction::BOTTOM);
    }
    if (static_cast<uint8_t>(newDirs) & static_cast<uint8_t>(Direction::LEFT)) {
      dfs(x, y - 1, Direction::LEFT);
    }
    if (static_cast<uint8_t>(newDirs) &
        static_cast<uint8_t>(Direction::RIGHT)) {
      dfs(x, y + 1, Direction::RIGHT);
    }
  }

public:
  void run(const size_t x = 0, const size_t y = 0,
           const Direction dir = Direction::RIGHT) {
    dfs(x, y, dir);
  }

  void reset() {
    data.clear();
    data.resize(width * width);
  }

  size_t visited() const {
    return width * width - std::count(data.begin(), data.end(), 0);
  }
};

size_t part01(const std::string &filename) {
  const auto map = Map::fromFile(filename);

  auto dfs = Dfs(map);
  dfs.run();
  const auto visited = dfs.visited();
  return visited;
}

size_t part02Naive(const std::string &filename) {
  const auto map = Map::fromFile(filename);
  const size_t size = map.size();

  size_t maxVisited = 0;
  auto dfs = Dfs(map);

  // yes, I could probably generalize this, but I don't care now
  for (size_t i = 0; i < size; ++i) {
    dfs.reset();
    dfs.run(i, 0, Direction::RIGHT);
    maxVisited = std::max(maxVisited, dfs.visited());

    dfs.reset();
    dfs.run(i, size - 1, Direction::LEFT);
    maxVisited = std::max(maxVisited, dfs.visited());

    dfs.reset();
    dfs.run(0, i, Direction::BOTTOM);
    maxVisited = std::max(maxVisited, dfs.visited());

    dfs.reset();
    dfs.run(size - 1, i, Direction::TOP);
    maxVisited = std::max(maxVisited, dfs.visited());
  }
  return maxVisited;
}

int main() {
  std::string fileTest = "input_test.txt";
  std::string fileProd = "input_prod.txt";
  assert(part01(fileTest) == 46);
  std::cout << "Part 01: " << part01(fileProd) << std::endl;
  assert(part02Naive(fileTest) == 51);
  std::cout << "Part 02: " << part02Naive(fileProd) << std::endl;
}
