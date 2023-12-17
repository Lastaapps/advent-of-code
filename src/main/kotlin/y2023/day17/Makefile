GCC = g++
GCC_OPTIONS = -Wall -pedantic -g3 -fsanitize=address
# GCC_OPTIONS = -Wall -pedantic -Ofast

.PHONY: run day17
.DEFAULT_GOAL: run

run: day17 Makefile
	@ time ./day17

day17: day17.cpp
	$(GCC) $(GCC_OPTIONS) -o $@ $^
