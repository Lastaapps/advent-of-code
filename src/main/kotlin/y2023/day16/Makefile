
.DEFAULT_TARGET=run

run: day16 Makefile
	@ ./day16

day16: day16.cpp
	# I don't want to spent few minutes searching for the correct config, sorry
	g++ -Wall -pedantic -g3 -fsanitize=address -o day16 day16.cpp
