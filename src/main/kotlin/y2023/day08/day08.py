#!/bin/python

from typing import Dict, Tuple, List, Set, Union
from itertools import product, combinations
from math import gcd, lcm, sqrt
from functools import reduce

input = ["input_test_1.txt", "input_test_2.txt", "input_test_3.txt", "input_prod.txt"]
input = input[3]

input = open(input, "r").read()
input = input.split("\n")

commands = input[0]
graph: Dict[str, Tuple[str, str]] = {}
for line in input[2:-1]:
    parts = line.split("=")
    key = parts[0].strip()
    options = parts[1].strip(" ()").split(",")
    left = options[0].strip()
    right = options[1].strip()
    graph[key] = (left, right)


def find_paths(locations: List[str], rule) -> int | None:
    """Yes, the most naive solution"""

    if locations[0] not in graph.keys():
        return None

    steps = 0
    found = False
    while not found:
        for order in commands:
            new_locations: List[str] = []
            found = True

            for location in locations:
                if not rule(location):
                    found = False

                data = graph[location]

                if order == "L":
                    new_locations.append(data[0])
                else:
                    new_locations.append(data[1])

            locations = new_locations

            if found == True:
                break

            steps += 1

    return steps


print("Part 01:", find_paths(["AAA"], lambda x: x == "ZZZ"))
# print("Part 02:", find_paths([x for x in graph.keys() if x[2] == "A"], lambda x: x[2] == "Z"))

# In part 2 I pretend that the input is general and I use CRT.
# First I find all the ends (distance from start) in complete loops
# And then generalized CRT.
# I got little stuck, those are sources of inspiration I used:
# My uni notes/resources
# https://math.stackexchange.com/questions/1644677/what-to-do-if-the-modulus-is-not-coprime-in-the-chinese-remainder-theorem
# https://programming.dev/comment/5554927


def detect_cycle_ends(start: str, is_end) -> (int, List[int]):
    """For each start find the internal loop and all the ends"""

    found_ends: List[int] = []
    visited: Dic[(int, str), int] = dict()
    steps = 0

    location = start

    while True:
        for index, order in enumerate(commands):
            entry = (index, location)
            if entry in visited.keys():
                return (steps - visited[entry], found_ends)
            visited[entry] = steps

            if is_end(location):
                found_ends.append(steps)

            # yes, I could have just turned it to true/false before. But no
            if order == "L":
                location = graph[location][0]
            else:
                location = graph[location][1]

            steps += 1


cycles = [
    detect_cycle_ends(x, lambda x: x[2] == "Z") for x in graph.keys() if x[2] == "A"
]
cycles = list(map(lambda x: [(end, x[0]) for end in x[1]], cycles))


def factorize(num: int) -> Set[int]:
    out: Set[int] = set()

    while num % 2 == 0:
        num /= 2
        out.add(2)

    n = 3
    while num != 1:
        if num % n == 0:
            num /= n
            out.add(n)
        else:
            n += 2

    return out


def solve_crt(ends: List[Tuple[int, int]]) -> Union[int, None]:
    # check solvable
    for a, b in combinations(ends, 2):
        if (a[0] - b[0]) % gcd(a[1], b[1]) != 0:
            return None

    def decompose_to_prime_modulos(x: Tuple[int, int]) -> List[Tuple[int, int]]:
        end, loop = x
        return {(end % prime, prime) for prime in factorize(loop)}

    equations = map(decompose_to_prime_modulos, ends)
    # flatten
    equations = [equation for group in equations for equation in group]

    if False:
        modulo = reduce(lambda x, y: lcm(x, y), map(lambda x: x[1], equations), 1)
    else:
        equations = set(equations)
        modulo = reduce(lambda x, y: x * y, map(lambda x: x[1], equations), 1)

    def resolve_equation(eq: Tuple[int, int]) -> int:
        end, loop = eq
        m = modulo // loop
        return end * m * (m % loop)

    res = sum(map(resolve_equation, equations)) % modulo
    if res <= 0:
        res += modulo

    return res


res = min(list(filter(None, map(lambda x: solve_crt(list(x)), product(*cycles)))))
print("Part 02:", res)
assert res == 21366921060721
