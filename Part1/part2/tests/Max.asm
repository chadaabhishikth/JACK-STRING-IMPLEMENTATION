// Computes R2 = max(R0, R1)  where R0, R1 >= 0

   @R0
   D=M              // D = R0
   @R1
   D=D-M            // D = R0 - R1
   @OUTPUT_FIRST
   D;JGT            // if R0 > R1 goto OUTPUT_FIRST
   @R1
   D=M              // D = R1
   @OUTPUT_D
   0;JMP            // goto OUTPUT_D
(OUTPUT_FIRST)
   @R0             
   D=M              // D = R0
(OUTPUT_D)
   @R2
   M=D              // M[2] = D (greatest value)
(INFINITE_LOOP)
   @INFINITE_LOOP
   0;JMP            // infinite loop
