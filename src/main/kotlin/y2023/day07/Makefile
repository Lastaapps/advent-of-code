COBC=cobc # Uses GnuCOBOL

.DEFAULT_GOAL := run
.PHONY: build run debug translate

build: day07

run: day07 Makefile
	./day07

debug: day07 Makefile
	./day07

translate: day07.cob
	$(COBC) -C $^ 

day07: day07.cob
	$(COBC) -x -o $@ $^ 

