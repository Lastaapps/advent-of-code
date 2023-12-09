#!/bin/python

from typing import Dict, Tuple, List, Set

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

                if order == 'L':
                    new_locations.append(data[0])
                else:
                    new_locations.append(data[1])

            locations = new_locations

            if found == True:
                break

            steps += 1

    return steps

print(find_paths(["AAA"], lambda x: x == "ZZZ"))
# The second part is to slow as I would not guess from the assignment that
# the ghosts go in circles and I don't want to just copy idea of other people.
# I should be probably able to apply Chinese remainder theorem or something
# on it, but I don't have time for this right now, so maybe later.
# print(find_paths([x for x in graph.keys() if x[2] == "A"], lambda x: x[2] == "Z"))

