#!/bin/python

from typing import Dict, Tuple

input = ["input_test_1.txt", "input_test_2.txt", "input_prod.txt"]
input = input[2]

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

location = "AAA"
steps = 0
found = False
while not found:
    for order in commands:
        if location == "ZZZ":
            found = True
            break
        data = graph[location]
        if order == 'L':
            location = data[0]
        else:
            location = data[1]
        steps += 1

print(steps)
