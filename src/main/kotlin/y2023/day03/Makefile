HCC = hcc

.DEFAULT_GOAL := run
.PHONY: build run debug

build: day03

run: day03 Makefile
	./day03

debug: day03 Makefile
	HCC_DEBUG=1 ./day03

day03: day03.hc
	$(HCC) $^ --out $@ 

