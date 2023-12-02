.global _start      # Provide program starting address to linker

# Setup the parameters to print hello world
# and then call Linux to do it.

.macro push (%reg)
	addi	sp, sp, -4
	sw	%reg, (sp)
.end_macro
.macro pop (%reg)
	lw	%reg, (sp)
	addi	sp, sp, +4
.end_macro
.macro scall (%label)
	push	ra
	call	%label
	pop	ra
.end_macro
.macro max (%baseline, %challenger)
	blt	%challenger %baseline, max_done
	mv	%baseline, %challenger
max_done:
.end_macro

_start: 
	#addi  a0, x0, 1		# 1 = StdOut
        #la    s9, input		# load address of helloworld
        #addi  a2, x0, 13	# length of our string
        #addi  a7, x0, 64	# linux write system call
        #ecall			# Call linux to output the string

	scall	main
	
	# Setup the parameters to exit the program
	# and then call Linux to do it
        addi	a7, x0, 93	# Service command code 93 terminates
        ecall			# Call linux to terminate the program




main:
	push	s2
	push	s3
	push	s9


	li	s2, 0	# global id sum
	li	s3, 0	# global powers sum
	la	s9, input
main_loop:
	lbu	t0, (s9)	# load next char
	beqz 	t0, main_done
	scall	process_game
	add	s2, s2, a0
	add	s3, s3, a1
	j	main_loop
main_done:
	mv	a0, s2
	mv	a1, s3
	pop	s9
	pop	s3
	pop	s2
	ret



# s9 - string pointer
# a0 - returns value of the game, 0 for invalid one
# a1 - returns power of the smallet possible game
process_game:
	push	s0		# Game ID/value
	push	s1		# Validity
	push	s2		# Max red
	push	s3		# Max green
	push	s4		# Max blue

	addi	s9, s9, 5	# Skip Game prefix
	scall	read_number
	mv	s0, a0		# Store ID to s0
	addi	s9, s9, 2	# skip ": "
	li	s1, 1		# store if the game is valid, true by default
	li	s2, 0
	li	s3, 0
	li	s4, 0

process_game_round:
process_game_color:
	scall	process_color
	and	s1, s1, a0
	
	lbu	t0, (s9)	# load next char
	li	t3, ','
	beq 	t0, t3 process_game_next_color
	li	t3, ';'
	beq 	t0, t3 process_game_next_round
	j	process_game_resolve	# must be \n
	
process_game_next_color:
	addi	s9, s9, 2	# skip ", "
	j	process_game_color

process_game_next_round:
	addi	s9, s9, 2	# skip "; "
	j	process_game_round

process_game_resolve:
	bnez 	s1, process_game_end	# check if game is valid (0 == invalid)
	li	s0, 0		# reset game value as it is invalid

process_game_end:
	mv	a0, s0
	mul	s2, s2, s3
	mul	a1, s2, s4
	addi	s9, s9, 1	# skip "\n"
	pop	s4
	pop	s3
	pop	s2
	pop	s1
	pop	s0
	ret
	

# s9 - address to read the number from (will be shifted)
# a0 - output register
read_number:
	li	a0, 0		# reset a0
read_number_loop:
	lbu	t0, (s9)	# load next char
	addi	t0, t0, -48	# 48 is ascii value for '0'
	li	t3, 10
	bgeu  	t0, t3, read_number_end	# the char is a digit
	# times 10
	add 	a0, a0, a0	# * 2
	mv	t3, a0
	add 	a0, a0, a0	# * 4
	add 	a0, a0, a0	# * 8
	add	a0, a0, t3	# * 8 + * 2
	
	add	a0, a0, t0
	addi	s9, s9, 1
	j	read_number_loop
read_number_end:
	ret

# s9 - address to read the number from (will be shifted)
# s2 - max red
# s3 - max green
# s4 - max blue
# a0 - in the color reading is valid
process_color:
	scall	read_number
	addi	s9, s9, 1	# skip space
	lbu	t0, (s9)	# load next char
	li	t3, 'r'
	beq 	t0, t3 process_color_red
	li	t3, 'g'
	beq 	t0, t3 process_color_green
	li	t3, 'b'
	beq 	t0, t3 process_color_blue
	ret

process_color_red:
	addi	s9, s9, 3	# skip color name chars
	max	s2, a0
	sltiu 	a0, a0, 13
	ret
process_color_green:
	addi	s9, s9, 5	# skip color name chars
	max	s3, a0
	sltiu 	a0, a0, 14
	ret
process_color_blue:
	addi	s9, s9, 4	# skip color name chars
	max	s4, a0
	sltiu 	a0, a0, 15
	ret

.data

# input:	.asciz 	"Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green\n"
# input:	.asciz	"Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green\nGame 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue\nGame 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red\nGame 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red\nGame 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green"
input:	.asciz	"Game 1: 1 green, 6 red, 4 blue; 2 blue, 6 green, 7 red; 3 red, 4 blue, 6 green; 3 green; 3 blue, 2 green, 1 red\nGame 2: 2 blue, 4 red, 7 green; 17 red, 3 blue, 2 green; 3 green, 14 red, 1 blue\nGame 3: 12 blue, 3 red, 1 green; 8 blue, 9 red; 1 blue, 1 green, 9 red; 4 blue, 1 green, 9 red\nGame 4: 2 red, 10 green, 5 blue; 11 blue, 4 green; 6 green, 7 blue, 2 red; 4 blue, 9 green; 6 green, 1 red, 5 blue\nGame 5: 10 green, 5 blue, 5 red; 10 blue, 13 green; 2 red, 12 blue; 9 green, 9 red\nGame 6: 2 red, 3 green; 1 blue, 15 red, 2 green; 1 green, 7 red\nGame 7: 16 blue, 4 green, 9 red; 6 red, 2 blue, 12 green; 2 red, 5 green, 14 blue; 11 blue, 13 red; 10 blue, 3 red, 17 green; 1 green, 12 blue\nGame 8: 14 red, 12 green, 1 blue; 5 blue, 7 green, 12 red; 8 green, 1 red, 8 blue; 8 blue, 2 green, 15 red; 9 blue, 12 red, 10 green; 4 blue, 15 red, 1 green\nGame 9: 2 red, 7 green, 5 blue; 1 red, 5 blue, 13 green; 5 blue\nGame 10: 4 red, 1 green, 4 blue; 7 green, 8 blue, 4 red; 9 green, 3 red, 8 blue; 5 red, 2 green, 7 blue\nGame 11: 4 green, 1 blue, 1 red; 3 green, 3 red, 1 blue; 3 green, 1 red, 1 blue\nGame 12: 7 red, 6 green, 12 blue; 6 blue, 8 green, 3 red; 12 green, 5 blue, 4 red; 3 red, 16 blue, 8 green; 12 red, 11 green, 6 blue\nGame 13: 2 green, 5 red, 12 blue; 8 green, 12 red, 4 blue; 6 blue, 7 green, 13 red\nGame 14: 1 blue, 7 green, 5 red; 1 blue, 8 green, 6 red; 3 green, 1 blue, 4 red\nGame 15: 11 red, 8 blue, 1 green; 11 red, 1 green; 3 green, 8 red, 2 blue; 4 blue, 11 red, 1 green; 5 blue, 5 red, 2 green\nGame 16: 18 green, 4 blue, 2 red; 5 blue, 11 green, 10 red; 8 red, 2 blue, 14 green; 8 red, 7 blue, 1 green; 3 red, 5 blue, 17 green; 6 blue, 5 green, 11 red\nGame 17: 3 blue, 3 red, 7 green; 4 blue, 1 red, 2 green; 5 blue, 3 green, 3 red\nGame 18: 2 blue, 2 red, 1 green; 4 blue, 2 red, 7 green; 10 blue, 4 red, 3 green; 5 blue, 3 red, 2 green; 4 green, 3 red, 4 blue; 3 green, 5 red, 5 blue\nGame 19: 2 red, 1 green, 1 blue; 8 red, 8 blue, 10 green; 16 green, 5 blue, 2 red; 4 red, 9 green\nGame 20: 12 red, 1 blue, 1 green; 4 blue, 2 green, 2 red; 3 blue; 5 red, 8 green; 14 red, 4 blue, 6 green\nGame 21: 9 red, 7 green, 1 blue; 5 green, 17 red, 11 blue; 14 red, 7 blue, 10 green; 7 green, 7 red, 10 blue; 6 blue, 6 green, 17 red; 16 red, 13 green, 7 blue\nGame 22: 4 blue, 1 red; 1 green, 8 blue; 1 green; 6 blue, 1 red\nGame 23: 13 red, 7 blue, 1 green; 4 green, 2 blue, 7 red; 4 green, 10 blue, 12 red\nGame 24: 9 green, 10 blue; 2 blue, 4 green, 4 red; 9 green, 1 red, 9 blue; 4 green, 5 red, 12 blue\nGame 25: 4 red, 1 green; 10 green, 6 red, 4 blue; 4 red, 1 blue, 7 green; 10 green, 3 red, 7 blue\nGame 26: 8 red, 1 green, 2 blue; 5 green, 5 red; 6 green, 19 red; 11 red, 2 blue, 8 green; 13 red, 2 blue, 5 green; 15 red, 2 blue, 10 green\nGame 27: 17 blue, 1 green; 2 red, 12 blue, 11 green; 16 green, 16 blue; 18 green, 4 blue; 10 blue, 1 red, 8 green\nGame 28: 5 red, 1 green, 1 blue; 3 blue, 8 green, 4 red; 6 green, 2 red, 2 blue\nGame 29: 3 green, 12 red, 11 blue; 2 green, 15 red, 8 blue; 13 red, 4 green; 17 red, 9 blue, 5 green\nGame 30: 10 green; 4 blue, 1 green; 2 blue, 2 red, 7 green; 5 green, 4 blue, 1 red; 4 red, 10 green, 1 blue\nGame 31: 15 blue, 2 red; 17 blue, 2 green; 19 blue, 6 red\nGame 32: 1 green, 7 red; 8 red, 1 blue; 5 red, 1 blue, 11 green; 3 blue, 17 red\nGame 33: 11 red, 9 green, 1 blue; 3 green, 8 blue; 10 red, 4 green, 8 blue; 6 red, 9 blue, 17 green; 15 green, 10 red, 4 blue; 1 red, 2 blue, 7 green\nGame 34: 13 red, 6 green; 6 red, 14 green, 2 blue; 3 red, 19 green; 9 green, 9 red\nGame 35: 7 green, 3 red; 12 green, 7 blue; 13 green, 7 red, 6 blue; 3 blue, 12 red\nGame 36: 6 blue, 11 green, 14 red; 3 blue, 12 green, 4 red; 18 red, 1 blue; 7 red, 9 green, 6 blue\nGame 37: 3 red, 16 blue, 6 green; 2 green, 7 blue; 8 blue, 3 red\nGame 38: 16 blue, 3 green, 14 red; 8 red, 15 blue; 17 red, 15 blue, 4 green; 1 green, 11 blue, 17 red; 3 green, 10 blue, 17 red\nGame 39: 1 green, 2 red, 5 blue; 12 blue, 12 green; 3 blue, 1 red\nGame 40: 1 red, 2 blue, 1 green; 7 green, 1 red, 6 blue; 8 blue, 1 red, 6 green; 12 blue, 1 red, 3 green; 4 green, 8 blue\nGame 41: 2 red, 2 blue, 5 green; 5 red, 8 blue; 4 green, 4 blue; 1 red, 11 blue\nGame 42: 1 red, 3 green, 13 blue; 13 blue, 7 green; 13 green; 1 red, 3 blue, 4 green; 13 blue, 7 green\nGame 43: 3 red, 4 green; 7 red, 11 blue, 3 green; 3 green, 12 red, 7 blue; 9 blue, 5 green\nGame 44: 4 blue, 9 red, 2 green; 10 blue, 5 red, 2 green; 9 red, 9 blue, 1 green; 8 blue, 2 green, 14 red; 3 blue, 3 green, 6 red; 4 blue, 3 green, 14 red\nGame 45: 1 red, 2 green, 2 blue; 2 green, 1 red; 1 green, 2 blue; 1 green, 1 red, 2 blue; 2 red, 2 blue, 1 green\nGame 46: 1 green, 3 red, 3 blue; 6 green, 2 blue, 4 red; 1 green, 3 blue, 1 red; 3 green, 1 blue, 5 red; 6 green; 1 red, 1 green, 2 blue\nGame 47: 18 green, 1 red, 7 blue; 6 blue, 19 green, 1 red; 5 blue, 7 green, 1 red; 1 red, 5 blue, 16 green; 15 green, 3 blue\nGame 48: 4 green, 8 blue, 8 red; 13 green, 5 red, 12 blue; 9 red, 6 blue, 10 green; 18 green, 3 blue, 4 red; 2 blue, 9 red, 8 green\nGame 49: 9 blue, 5 red, 9 green; 5 blue, 11 green, 5 red; 12 green, 6 blue\nGame 50: 13 red, 8 green, 3 blue; 2 red, 11 green, 3 blue; 16 red, 7 green; 3 blue, 11 green, 15 red; 10 red, 2 blue, 5 green; 7 green, 2 blue, 4 red\nGame 51: 2 red, 1 green, 3 blue; 2 green, 11 red, 17 blue; 2 red, 3 green, 6 blue; 4 red, 3 green, 6 blue; 13 red, 12 blue\nGame 52: 1 blue, 5 green; 20 green, 6 blue; 9 blue, 6 green; 11 green, 1 red; 1 green, 1 red, 1 blue\nGame 53: 8 red, 6 blue; 6 blue, 6 red, 2 green; 5 blue, 2 green, 3 red; 3 green, 3 blue; 4 green, 5 red, 1 blue\nGame 54: 4 blue, 1 red, 3 green; 4 green, 10 blue, 9 red; 7 red, 3 blue, 3 green; 9 green, 9 red, 1 blue; 9 blue, 6 red, 7 green; 6 blue, 7 green, 9 red\nGame 55: 15 red, 1 blue, 6 green; 11 blue, 3 red; 9 blue, 3 red, 1 green\nGame 56: 8 green, 8 red, 9 blue; 8 red, 8 green, 1 blue; 7 red, 10 green, 4 blue; 10 blue, 2 green, 9 red\nGame 57: 10 red, 3 green, 2 blue; 1 red, 4 green; 7 red, 1 green, 3 blue; 12 red, 4 blue; 14 red, 5 green, 4 blue\nGame 58: 8 green, 3 blue, 7 red; 7 red, 14 blue, 5 green; 3 green, 7 red; 16 blue, 15 green; 1 red, 10 blue\nGame 59: 3 red, 13 green, 2 blue; 10 blue, 3 green, 6 red; 3 green, 2 blue; 7 green, 2 blue, 7 red; 17 green, 6 blue, 15 red\nGame 60: 2 blue, 2 red, 6 green; 11 green, 1 blue, 2 red; 1 blue, 9 green; 1 red, 4 green, 2 blue; 1 red, 2 blue, 10 green\nGame 61: 3 red, 12 blue, 1 green; 3 red, 1 green, 18 blue; 5 blue, 2 red\nGame 62: 4 red, 3 blue, 8 green; 2 blue, 8 red, 9 green; 8 blue, 15 green, 1 red\nGame 63: 14 green, 2 red, 1 blue; 7 green, 11 blue, 1 red; 7 blue, 3 red; 4 green, 10 blue, 3 red\nGame 64: 8 blue, 18 green, 2 red; 3 red, 17 green; 7 green, 1 red, 12 blue; 15 green, 2 red, 4 blue; 7 green, 8 red, 13 blue\nGame 65: 6 blue, 5 green, 2 red; 1 red, 4 green; 5 green, 1 blue; 6 blue, 3 red, 2 green; 4 blue, 5 green\nGame 66: 11 red, 9 blue, 4 green; 8 red, 8 blue; 9 red, 7 blue; 1 blue, 12 green, 4 red; 2 red, 11 blue, 10 green\nGame 67: 1 red, 4 blue, 1 green; 7 red, 1 blue; 3 green, 4 blue, 6 red; 6 green, 3 blue, 14 red; 11 red, 1 blue, 1 green; 4 green, 8 red\nGame 68: 3 red, 1 green, 2 blue; 1 red, 9 blue; 2 red, 1 green\nGame 69: 3 green, 2 blue, 2 red; 1 red, 6 green; 13 red, 2 blue, 4 green; 4 blue, 13 red, 6 green; 12 red, 2 blue\nGame 70: 15 blue, 2 green, 7 red; 3 red, 14 blue; 6 blue, 1 green; 1 red, 2 green, 4 blue; 2 green, 13 red; 12 blue, 3 red\nGame 71: 7 red, 3 blue; 1 red, 4 blue; 2 red, 5 green, 1 blue; 6 blue, 8 red, 1 green; 3 green, 7 blue, 8 red\nGame 72: 7 green; 4 green, 2 red, 8 blue; 1 blue, 5 green\nGame 73: 5 red, 5 green, 2 blue; 8 red, 1 blue, 8 green; 1 red, 3 blue, 7 green\nGame 74: 17 green, 9 blue, 4 red; 20 green, 2 red, 7 blue; 7 blue, 2 green, 4 red; 2 blue, 5 red, 20 green; 1 blue, 1 red, 12 green; 19 green, 9 blue, 3 red\nGame 75: 1 red, 8 green, 9 blue; 7 blue, 3 green, 1 red; 2 green, 1 red, 9 blue; 5 blue, 1 red, 8 green; 2 green, 1 red, 11 blue; 5 green, 1 red\nGame 76: 3 blue, 16 green, 2 red; 10 green, 3 blue, 1 red; 6 blue, 14 red, 13 green; 7 red, 2 green, 13 blue\nGame 77: 7 red, 14 green; 1 blue, 1 red; 4 red, 1 green; 7 green, 11 red\nGame 78: 1 red, 19 green; 10 green, 14 red, 1 blue; 3 green, 3 blue, 11 red; 7 blue, 1 green; 15 red, 3 green, 4 blue\nGame 79: 7 red, 7 green, 6 blue; 3 red, 7 green, 5 blue; 7 red, 8 green, 12 blue\nGame 80: 15 red, 6 blue; 1 red, 5 green, 2 blue; 1 green, 3 blue\nGame 81: 3 red, 7 blue, 7 green; 7 green, 2 blue, 4 red; 3 green, 5 blue; 9 blue, 3 red, 6 green; 6 green, 1 red, 3 blue; 8 blue, 2 green, 1 red\nGame 82: 5 red, 13 green; 3 blue, 13 green; 6 blue, 4 red, 10 green; 5 red, 1 green, 4 blue; 1 blue, 8 red; 4 red, 5 green\nGame 83: 17 red, 1 blue, 2 green; 3 green, 3 red, 2 blue; 1 red, 5 blue, 10 green; 4 blue, 9 red, 11 green\nGame 84: 13 green, 14 red, 12 blue; 14 blue, 2 red, 1 green; 4 blue, 8 red\nGame 85: 3 red, 1 blue; 6 red, 3 blue, 2 green; 5 green, 3 blue, 3 red; 3 green, 5 blue, 1 red; 1 blue, 12 red, 2 green\nGame 86: 16 blue, 17 green, 7 red; 14 blue, 13 green; 18 blue, 8 green\nGame 87: 1 blue, 1 red; 4 blue, 1 green, 4 red; 1 green, 16 red; 1 green, 12 red, 1 blue\nGame 88: 1 red, 6 green; 3 red, 2 blue, 19 green; 11 green, 2 red; 5 blue, 5 green; 5 blue, 9 green, 1 red; 2 blue, 2 red, 4 green\nGame 89: 4 green, 11 red; 8 blue, 14 red; 14 blue, 8 green, 9 red; 14 green, 15 red, 10 blue\nGame 90: 8 green, 2 red, 1 blue; 11 green, 4 blue, 2 red; 7 green, 2 blue; 13 green, 1 red\nGame 91: 1 blue, 3 green; 1 blue; 4 green, 1 blue, 1 red; 1 blue, 2 red; 1 green, 2 red; 2 red, 5 green, 2 blue\nGame 92: 16 red, 4 green, 5 blue; 9 blue, 13 green, 5 red; 13 red, 11 green, 7 blue; 11 red, 8 green, 2 blue\nGame 93: 4 blue, 3 red, 3 green; 4 blue, 2 red, 1 green; 1 green, 2 red, 2 blue; 1 green, 2 red, 2 blue; 4 green, 1 blue\nGame 94: 8 blue, 11 red, 7 green; 8 red, 6 green; 15 blue, 11 green, 2 red; 9 green, 6 red; 16 blue, 5 red, 7 green\nGame 95: 13 blue, 1 red, 10 green; 11 green, 9 blue; 6 blue\nGame 96: 1 green, 6 red; 1 red; 12 red, 1 green; 6 red, 1 blue\nGame 97: 1 red, 9 blue, 8 green; 2 green, 6 blue, 1 red; 6 green, 1 blue\nGame 98: 9 blue, 7 green, 8 red; 6 red, 11 blue, 4 green; 11 green, 9 blue, 15 red; 11 red, 6 blue, 16 green\nGame 99: 2 blue, 1 red, 9 green; 8 red, 1 blue, 1 green; 2 red, 7 green, 8 blue; 1 red, 5 green, 7 blue; 7 blue, 10 green, 9 red; 1 green, 1 blue, 1 red\nGame 100: 3 blue, 6 red, 9 green; 4 red, 3 green; 4 green, 16 red, 1 blue; 14 blue, 1 green\n"
