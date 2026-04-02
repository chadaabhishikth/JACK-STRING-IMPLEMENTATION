// Initialize i = 1
@i
M=1

// LOOP: if i == 5 stop
(LOOP)
@i
D=M
@5
D=D-A
@END
D;JEQ

// i = i + 1
@i
M=M+1

@LOOP
0;JMP

(END)
@END
0;JMP
