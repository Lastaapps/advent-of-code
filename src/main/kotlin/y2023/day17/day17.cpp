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
#include <set>
#include <string>
#include <string_view>
#include <unordered_set>
#include <utility>
#include <vector>

using Location = std::complex<int16_t>;
using Direction = std::complex<int16_t>;

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
    if (steps != other.steps)
      return steps < other.steps;
    // required only for backtracking to work properly
    // if (loc != other.loc)
    //   return LocationUtil{}(loc, other.loc);
    // if (dir != other.dir)
    //   return LocationUtil{}(dir, other.dir);
    return false;
  }

  bool operator>(const Position &other) const noexcept {
    if (dist != other.dist)
      return dist > other.dist;
    if (steps != other.steps)
      return steps > other.steps;
    // required only for backtracking to work properly
    // if (loc != other.loc)
    //   return LocationUtil{}(other.loc, loc);
    // if (dir != other.dir)
    //   return LocationUtil{}(other.dir, dir);
    return false;
  }
};

std::ostream &operator<<(std::ostream &out, const Position &pos) {
  return out << "{" << pos.dist << "," << pos.steps << ",pos:" << pos.loc
             << ",dir:" << pos.dir << "}";
}

template <uint32_t minStep, uint32_t maxStep, bool doBacktracking = false>
class Dijkstra final {
  const Map &map;
  const size_t width;
  const size_t area = width * width;
  std::vector<bool> data;
  std::vector<uint16_t> bestDistances;
  std::vector<Position> backtrack;

public:
  Dijkstra(const Map &map)
      : map(map), width(map.size()),
        data(std::vector<bool>(4 * maxStep * area)),
        bestDistances(std::vector<uint16_t>(4 * maxStep * area)),
        backtrack(
            std::vector<Position>(doBacktracking ? 4 * maxStep * area : 0)) {}

private:
  size_t hash(const Position &pos) const noexcept {
    uint8_t dirReal = static_cast<uint8_t>(pos.dir.real());
    uint8_t dirImag = static_cast<uint8_t>(pos.dir.imag());
    const size_t hashDir = ((dirReal | dirImag) & 0b10) | (dirImag & 0b01);

    const size_t hash = hashDir * maxStep * area + pos.steps * area +
                        pos.loc.imag() * width + pos.loc.real();
    return hash;
  }

  void visit(const Position &pos) {
    // data[hash(pos)] = true;

    // 70% speed increase
    for (uint16_t i = pos.steps; i < maxStep; ++i) {
      data[hash({pos.dist, i, pos.dir, pos.loc})] = true;
    }
  }

  bool at(const Position &pos) const { return data[hash(pos)]; }

  // 20% speed increase (could be more if I removed the default bit set)
  bool proposeDistance(const Position &pos) {
    const auto h = hash(pos);
    const auto soFarTheBest = bestDistances[h];
    if (soFarTheBest == 0 || pos.dist < soFarTheBest) {
      bestDistances[h] = pos.dist;
      return true;
    } else {
      return false;
    }
  }

  std::priority_queue<Position, std::vector<Position>, std::greater<Position>>
      queue;

  void insertStart(Position &&pos) {
    if constexpr (doBacktracking) {
      backtrack[hash(pos)] = pos;
    }

    insert<minStep>(pos, pos.dir, minStep - 1);
  }

  void insertBacktracking(const Position &prev, const Position &now) {
    if constexpr (!doBacktracking)
      return;

    auto &orginal = backtrack[hash(now)];

    if (orginal.dir == Direction(0, 0) || now < orginal) {
      backtrack[hash(now)] = prev;
    }
  }

  template <uint16_t moveScale>
  void insert(const Position &pos, const Direction &dir,
              const uint16_t newSteps) {
    const Location newLoc = pos.loc + dir * static_cast<int16_t>(moveScale);

    // I can't think of a good use for padding here
    if (newSteps >= maxStep || static_cast<uint16_t>(newLoc.real()) >= width ||
        static_cast<uint16_t>(newLoc.imag()) >= width) {
      return;
    }

    uint16_t newDist = pos.dist;
    for (size_t i = 1; i <= moveScale; ++i) {

      const uint16_t odlDist = newDist;

      newDist += map.at(pos.loc + dir * static_cast<int16_t>(i));

      if constexpr (doBacktracking) {
        const auto prev =
            i == 1 ? pos
                   : Position{
                         odlDist,
                         static_cast<uint16_t>(newSteps - (moveScale - i) - 1),
                         dir, pos.loc + dir * static_cast<int16_t>(i - 1)};

        const auto now =
            Position{newDist, static_cast<uint16_t>(newSteps - (moveScale - i)),
                     dir, pos.loc + dir * static_cast<int16_t>(i)};
        insertBacktracking(prev, now);
      }
    }
    const Position newPos = {newDist, newSteps, dir, newLoc};

    // just a little optimization (30% speed up)
    if (at(newPos)) {
      return;
    }

    if (proposeDistance(newPos)) {
        queue.emplace(std::move(newPos));
    }
  }

  void process(const Position &&pos) {
    insert<1>(pos, pos.dir, pos.steps + 1);
    insert<minStep>(pos, pos.dir * Direction(0, 1), minStep - 1);
    insert<minStep>(pos, pos.dir * Direction(0, -1), minStep - 1);
  }

  std::unordered_set<Location, LocationUtil> backtrackPos(Position pos) {
    // std::set<Location, LocationUtil> backtrackResult;
    std::unordered_set<Location, LocationUtil> backtrackResult;

    // std::cout << "Backtracking" << std::endl;
    while (true) {
      // std::cout << pos << std::endl;
      backtrackResult.insert(pos.loc);

      if (pos.loc == Location(0, 0)) {
        break;
      }
      pos = backtrack[hash(pos)];
    }

    return backtrackResult;
  }

  Position finalPosition;

public:
  uint16_t run(const Location pos) {

    insertStart({0, 0, Direction(1, 0), Location(0, 0)});
    insertStart({0, 0, Direction(0, 1), Location(0, 0)});

    const Location target = Location(width - 1, width - 1);

    while (!queue.empty()) {
      const auto item = std::move(queue.top());
      queue.pop();

      if (at(item)) {
        continue;
      }

      if (item.loc == target) {
        finalPosition = item;
        return item.dist;
      }

      visit(item);
      process(std::move(item));
    }

    return -1;
  }

  void showPath(std::ostream &out = std::cout) {
    if constexpr (!doBacktracking) {
      return;
    }

    const auto backtrackResult = backtrackPos(finalPosition);
    for (size_t i = 0; i < width; ++i) {
      for (size_t j = 0; j < width; ++j) {
        const auto loc = Location(i, j);
        out << (char)(backtrackResult.count(loc) ? 'O' : '~');
      }
      out << "\n";
    }
  }
};

template <uint32_t minStep, uint32_t maxStep, bool doBacktracking = false>
size_t part0X(const std::string &filename) {
  const auto map = Map::fromFile(filename);
  auto dijikstra = Dijkstra<minStep, maxStep, doBacktracking>(map);
  const auto res = dijikstra.run(Location(0, 0));
  dijikstra.showPath();
  return res;
}

size_t part01(const std::string &filename) {
  return part0X<1, 3, true>(filename);
}

size_t part02(const std::string &filename) {
  return part0X<4, 10, true>(filename);
}

int main() {
  std::string fileTest = "input_test.txt";
  std::string fileProd = "input_prod.txt";

  assert(part01(fileTest) == 102);
  const auto part01Res = part01(fileProd);
  std::cout << "Part 01: " << part01Res << std::endl;
  assert(part01Res == 959);

  assert(part02(fileTest) == 94);
  const auto part02Res = part02(fileProd);
  std::cout << "Part 02: " << part02Res << std::endl;
  assert(part02Res == 1135);
}
