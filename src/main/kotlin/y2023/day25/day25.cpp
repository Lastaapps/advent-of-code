
#include <cassert>
#include <fstream>
#include <functional>
#include <iomanip>
#include <iostream>
#include <istream>
#include <queue>
#include <unordered_map>
#include <vector>

using Id = size_t;
using Graph = std::vector<std::vector<Id>>;

// yes, I should use more templates to make this faster, but this is safer
template <typename Key, typename Value>
Value getOrPut(std::unordered_map<Key, Value> &map, const Key &key,
               std::function<Value()> value) noexcept {
  auto entry = map.find(key);
  if (entry != map.end()) {
    return entry->second;
  } else {
    Value newVal = value();
    map.emplace(std::make_pair(key, newVal));
    return newVal;
  }
}

template <typename T>
std::ostream &operator<<(std::ostream &out, const std::vector<T> &data) {
  out << '[';
  bool isFirst = true;
  for (const T &item : data) {
    if (isFirst) {
      isFirst = false;
    } else {
      out << ',';
    }
    out << item;
  }
  return out << ']';
}

Graph parseInput(std::istream &in) {
  // TODO GNU maps
  std::unordered_map<std::string, Id> translate;
  Id nextId = 0;

  Graph graph;

  in >> std::noskipws;
  while (in) {
    char skip;
    const size_t charsCnt = 3 + 1;
    char id[charsCnt];
    in >> std::setw(charsCnt) >> id >> skip >> std::ws;

    if (!in) {
      break;
    }
    assert(skip == ':');

    const auto updateNodeCnt = [&nextId, &graph]() {
      graph.push_back(std::vector<Id>{});
      return nextId++;
    };
    const Id v = getOrPut<std::string, Id>(translate, id, updateNodeCnt);

    while (in >> std::setw(charsCnt) >> id >> skip) {
      const Id u = getOrPut<std::string, Id>(translate, id, updateNodeCnt);
      graph[v].push_back(u);
      graph[u].push_back(v);
      if (skip != ' ') {
        break;
      }
    }
  }

  return graph;
}

Graph parseInputFromFile(const std::string &filename) {
  auto stream = std::ifstream(filename);
  return parseInput(stream);
}

class Flow final {
  size_t size;
  std::vector<bool> data;

public:
  Flow(size_t size) : size(size), data(size * size) {}

  size_t getReserve(Id v, Id u) const {
    return (1 - data[v * size + u]) + data[u * size + v];
  }

  void sendFlow(Id v, Id u) {
    if (data[u * size + v]) {
      data[u * size + v] = 0;
    } else {
      data[v * size + u] = 1;
    }
  }
};

void backtrack(const std::vector<Id> &predecesors, Flow &flow, Id u) {
  while (true) {
    Id v = predecesors[u];
    if (v == u) {
      return;
    }

    flow.sendFlow(v, u);
    u = v;
  };
}

bool fordFulkerson(const Graph &graph, Flow &flow, Id from, Id to) {
  // TODO GNU
  std::queue<Id> queue{};
  auto predecesors = std::vector<Id>(graph.size(), Id(-1));
  queue.emplace(from);
  predecesors[from] = from;

  while (!queue.empty()) {
    const Id v = queue.front();
    queue.pop();

    for (const auto &u : graph[v]) {
      if (predecesors[u] != Id(-1)) {
        continue;
      }

      if (flow.getReserve(v, u) == 0) {
        continue;
      }

      predecesors[u] = v;
      if (u == to) {
        backtrack(predecesors, flow, to);
        return true;
      }

      queue.emplace(u);
    }
  }
  return false;
}

size_t findComponentSize(const Graph &graph, const Flow &flow, Id from) {
  std::queue<Id> queue{};
  auto visited = std::vector<Id>(graph.size());
  size_t visitedCnt = 0;

  queue.emplace(from);
  visited[from] = from;
  visitedCnt++;

  while (!queue.empty()) {
    const Id v = queue.front();
    queue.pop();

    for (const auto &u : graph[v]) {
      if (visited[u]) {
        continue;
      }

      if (flow.getReserve(v, u) == 0) {
        continue;
      }

      visited[u] = v;
      queue.emplace(u);
      visitedCnt++;
    }
  }

  return visitedCnt;
}

size_t part01(const std::string &filename) {
  const Graph graph = parseInputFromFile(filename);

  for (Id to = 1; to < graph.size(); ++to) {
    Flow flow = {graph.size()};

    fordFulkerson(graph, flow, 0, to);
    fordFulkerson(graph, flow, 0, to);
    fordFulkerson(graph, flow, 0, to);

    if (fordFulkerson(graph, flow, 0, to)) {
      // nodes are in the same component
      continue;
    }

    size_t comp1 = findComponentSize(graph, flow, 0);
    size_t comp2 = graph.size() - comp1;
    return comp1 * comp2;
  }

  std::cout << graph << std::endl;
  return 0;
}

size_t part02(const std::string &filename) { return 0; }

int main() {
  std::string fileTest = "input_test.txt";
  std::string fileProd = "input_prod.txt";

  assert(part01(fileTest) == 54);
  const size_t part01Res = part01(fileProd);
  std::cout << "Part 01: " << part01Res << std::endl;
  assert(part01Res == 613870);

  assert(part02(fileTest) == 0);
  const size_t part02Res = part02(fileProd);
  std::cout << "Part 02: " << part02Res << std::endl;
  assert(part02Res == 0);

  return 0;
}
