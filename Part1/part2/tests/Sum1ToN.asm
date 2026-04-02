// Computes R1 = 1 + 2 + ... + R0
// (Sum 1 to n, stores result in RAM[1])

   @R0
   D=M         // D = n
   @sum
   M=0         // sum = 0
   @i
   M=1         // i = 1
(LOOP)
   @i
   D=M         // D = i
   @R0
   D=D-M       // D = i - n
   @END
   D;JGT       // if (i - n) > 0, goto END
   @i
   D=M         // D = i
   @sum
   M=D+M       // sum = sum + i
   @i
   M=M+1       // i = i + 1
   @LOOP
   0;JMP       // goto LOOP
(END)
   @sum
   D=M
   @R1
   M=D         // RAM[1] = sum
(STOP)
   @STOP
   0;JMP       // infinite loop
