GCC = g++
# GCC_OPTIONS = -Wall -pedantic -g3 -fsanitize=address -ftrapv -Wstrict-overflow
GCC_OPTIONS = -Wall -pedantic -Ofast

.PHONY: run day18
.DEFAULT_GOAL: run

run: day18 Makefile
	@ time ./day18

day18: day18.cpp
	$(GCC) $(GCC_OPTIONS) -o $@ $^
