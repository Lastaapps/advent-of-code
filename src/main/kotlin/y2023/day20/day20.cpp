#include <algorithm>
#include <cassert>
#include <complex>
#include <cstdint>
#include <fstream>
#include <functional>
#include <iostream>
#include <numeric>
#include <queue>
#include <string>
#include <string_view>
#include <unordered_map>
#include <utility>
#include <variant>
#include <vector>

// ----------------------------------------------------------------------------
// yes, I should use more templates to make this faster, but this is safer
template <typename Key, typename Value>
Value getOrPut(std::unordered_map<Key, Value> &map, const Key &key,
               std::function<Value()> value) {
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
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
using Id = uint16_t;
enum class Level {
  LOW = 0,
  HIGH = 1,
};
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
Level constexpr otherLevel(const Level l) {
  return static_cast<Level>(l == Level::LOW);
}
std::ostream &operator<<(std::ostream &out, const Level &level) {
  return out << (level == Level::LOW ? "Low" : "High");
}
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
struct Signal {
  Id from, to;
  Level level;
};
std::ostream &operator<<(std::ostream &out, const Signal &signal) {
  return out << signal.from << "->" << signal.to << ' ' << signal.level;
}
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
struct NetworkState final {
  std::queue<Signal> queue{};
  std::complex<uint64_t> used = 0;
  uint64_t charge = 0;
};
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
struct Node {
  Id id;
  std::vector<Id> targets;

protected:
  Node(Id id, std::vector<Id> &&targets)
      : id(id), targets(std::move(targets)) {}

  void sendToAll(NetworkState &state, const Level l) {
    for (const auto &target : targets) {
      state.queue.emplace(Signal{id, target, l});
    }
  }
  virtual ~Node() = default;
  virtual std::string name() const { return "Node"; }

public:
  virtual void process(NetworkState &state, const Signal &&signal) {}

  friend std::ostream &operator<<(std::ostream &out, const Node &node);
};
std::ostream &operator<<(std::ostream &out, const Node &node) {
  return out << node.name() << '(' << node.id << ',' << node.targets << ')';
}
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
class Broadcast final : public Node {

public:
  Broadcast(Id id, std::vector<Id> &&targets) : Node(id, std::move(targets)) {}
  std::string name() const override { return "Broadcast"; }

  void process(NetworkState &state, const Signal &&signal) override {
    sendToAll(state, Level::LOW);
  };
};
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
class FlipFlop final : public Node {
  Level local = Level::LOW;

public:
  FlipFlop(Id id, std::vector<Id> &&targets) : Node(id, std::move(targets)) {}
  std::string name() const override { return "FlipFlop"; }

  void process(NetworkState &state, const Signal &&signal) override {
    if (signal.level == Level::HIGH) {
      return;
    }
    local = otherLevel(local);
    state.charge +=
        static_cast<int8_t>(local) - static_cast<int8_t>(otherLevel(local));

    sendToAll(state, local);
  };
};
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
class Conjunction final : public Node {
  std::unordered_map<Id, Level> levels{};
  uint16_t levelsOn = 0;

public:
  Conjunction(Id id, std::vector<Id> &&targets)
      : Node(id, std::move(targets)) {}
  std::string name() const override { return "Conjunction"; }

  void process(NetworkState &state, const Signal &&signal) override {
    const Level stored = levels.find(signal.from)->second;
    const Level input = signal.level;
    if (input != stored) {
      const int8_t diff =
          static_cast<int8_t>(input) - static_cast<int8_t>(otherLevel(input));
      state.charge += diff;
      levelsOn += diff;
      levels[signal.from] = input;
    }

    sendToAll(state, levelsOn == levels.size() ? Level::LOW : Level::HIGH);
  };

  void registerInRef(Id from) {
    levels.emplace(std::make_pair(from, Level::LOW));
  }
};
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
class Dummy final : public Node {

public:
  Dummy() : Node(-1, std::vector<Id>{}) {}
  std::string name() const override { return "Dummy"; }

  void process(NetworkState &state, const Signal &&signal) override{};
};
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
using NodeVariant = std::variant<Dummy, Broadcast, FlipFlop, Conjunction>;
std::ostream &operator<<(std::ostream &out, const NodeVariant &target) {
  if (std::holds_alternative<Dummy>(target)) {
    return out << std::get<Dummy>(target);
  } else if (std::holds_alternative<Broadcast>(target)) {
    return out << std::get<Broadcast>(target);
  } else if (std::holds_alternative<FlipFlop>(target)) {
    return out << std::get<FlipFlop>(target);
  } else if (std::holds_alternative<Conjunction>(target)) {
    return out << std::get<Conjunction>(target);
  } else {
    return out;
  }
}
// ----------------------------------------------------------------------------

std::vector<NodeVariant> parseInput(std::istream &in) {
  std::unordered_map<std::string, Id> translate;
  Id nextId = 0;

  translate.emplace(std::make_pair("broadcaster", nextId++));
  std::vector<NodeVariant> data;

  while (in) {
    char skip;
    std::string id;
    in >> id >> std::ws >> skip >> skip;
    if (!in) {
      break;
    }

    std::string trimmedId;
    if (id[0] == '&' || id[0] == '%') {
      trimmedId = id.substr(1);
    } else {
      trimmedId = id;
    }
    const Id numericId = getOrPut<std::string, Id>(
        translate, trimmedId, [&nextId]() { return nextId++; });

    std::vector<Id> targets;
    std::string targetId;
    while (in >> targetId) {
      const bool isLast = targetId[targetId.size() - 1] != ',';
      if (!isLast) {
        targetId = targetId.substr(0, targetId.size() - 1);
      }
      targets.emplace_back(getOrPut<std::string, Id>(
          translate, targetId, [&nextId]() { return nextId++; }));
      if (isLast) {
        break;
      }
    }

    if (numericId >= data.size()) {
      data.resize(numericId + 1);
    }

    if (id[0] == '%') {
      data[numericId] = FlipFlop(numericId, std::move(targets));
    } else if (id[0] == '&') {
      data[numericId] = Conjunction(numericId, std::move(targets));
    } else {
      data[numericId] = Broadcast(numericId, std::move(targets));
    }
  }

  if (nextId > data.size()) {
    data.resize(nextId);
  }

  std::vector<uint16_t> inputCnt(data.size());
  for (NodeVariant &target : data) {
    Id id;
    std::vector<Id> *targets;
    if (std::holds_alternative<Dummy>(target)) {
      id = std::get<Dummy>(target).id;
      targets = &std::get<Dummy>(target).targets;
    } else if (std::holds_alternative<Broadcast>(target)) {
      id = std::get<Broadcast>(target).id;
      targets = &std::get<Broadcast>(target).targets;
    } else if (std::holds_alternative<FlipFlop>(target)) {
      id = std::get<FlipFlop>(target).id;
      targets = &std::get<FlipFlop>(target).targets;
    } else if (std::holds_alternative<Conjunction>(target)) {
      id = std::get<Conjunction>(target).id;
      targets = &std::get<Conjunction>(target).targets;
    }

    for (auto index : *targets) {
      auto &dest = data[index];
      if (std::holds_alternative<Conjunction>(dest)) {
        std::get<Conjunction>(dest).registerInRef(id);
      }
    }
  }

  return data;
}
std::vector<NodeVariant> parseInputFromFile(const std::string &filename) {
  auto stream = std::ifstream(filename);
  return parseInput(stream);
}

class Logistics final {
  std::vector<NodeVariant> data;
  NetworkState state;
  std::vector<std::complex<uint64_t>> history;

public:
  Logistics(std::vector<NodeVariant> &&data) : data(data) {}

private:
  void iteratrion() {
    state.queue.push(Signal{static_cast<Id>(-1), 0});

    while (!state.queue.empty()) {
      const Signal signal = state.queue.front();
      state.queue.pop();
      state.used +=
          std::complex(static_cast<uint8_t>(signal.level),
                       static_cast<uint8_t>(otherLevel(signal.level)));

      // std::cout << "Processing signal " << signal << '\n';

      NodeVariant &target = data[signal.to];

      if (std::holds_alternative<Dummy>(target)) {
        std::get<Dummy>(target).process(state, std::move(signal));
      } else if (std::holds_alternative<Broadcast>(target)) {
        std::get<Broadcast>(target).process(state, std::move(signal));
      } else if (std::holds_alternative<FlipFlop>(target)) {
        std::get<FlipFlop>(target).process(state, std::move(signal));
      } else if (std::holds_alternative<Conjunction>(target)) {
        std::get<Conjunction>(target).process(state, std::move(signal));
      }
    }
  }

public:
  size_t run(size_t cycles = 1'000) {
    do {
      // std::cout << "Iteration " << history.size() << ", charge " << state.charge << '\n';
      state.used = 0;
      iteratrion();
      history.push_back(state.used);
    } while (state.charge != 0 && history.size() < cycles);

    auto base = std::reduce(history.begin(), history.end());
    base *= (cycles / history.size());
    const size_t index = cycles % history.size();
    if (index > 0) {
      base += history[index - 1];
    }
    return base.real() * base.imag();
  }
};

size_t part01(const std::string &filename) {
  auto input = parseInputFromFile(filename);
  // std::cout << "Input " << input << std::endl;

  return Logistics(std::move(input)).run();
}

int main() {
  std::string fileTest01 = "input_test_01.txt";
  std::string fileTest02 = "input_test_02.txt";
  std::string fileProd = "input_prod.txt";

  assert(part01(fileTest01) == 32000000);
  assert(part01(fileTest02) == 11687500);
  const size_t part01Res = part01(fileProd);
  std::cout << "Part 01: " << part01Res << std::endl;
  assert(part01Res == 866435264);

  return 0;
}
