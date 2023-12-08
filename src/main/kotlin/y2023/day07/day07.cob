      * https://www.mainframestechhelp.com/tutorials/cobol
       IDENTIFICATION DIVISION.
       PROGRAM-ID. DAY_07.
       AUTHOR. "Petr Laštovička (LastaApps)".
       DATE-WRITTEN. 12/07/2023.

       ENVIRONMENT DIVISION.
        CONFIGURATION SECTION. 
      * SOURCE-COMPUTER. IBM3278 WITH DEBUGGING MODE.
        INPUT-OUTPUT SECTION.
        FILE-CONTROL.
        SELECT CARDS           ASSIGN TO DISK
      *    "input_test.txt"
           "input_prod.txt"
            ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TRANSFORMED     ASSIGN TO DISK "transformed.txt"
      *        I did not get this working, idk, it does not matter
      *        ORGANIZATION IS LINE SEQUENTIAL
               .
           SELECT WORKFILE        ASSIGN TO workfile.

       DATA DIVISION.
       FILE SECTION.
      * File with input card values
       FD CARDS
           RECORD CONTAINS 7 TO 10 CHARACTERS
           DATA RECORD IS IN-HAND-REC
           RECORDING MODE F.
       01 IN-HAND-REC.
           05 IN-HAND-ID             PIC X(5).
           05 IN-HAND-FILL           PIC X(1).
           05 IN-HAND-SCORE          PIC 9(4).

      * File with transformed cards (debug only)
       FD TRANSFORMED
           RECORD CONTAINS 12 CHARACTERS
           DATA RECORD TRAN-HAND-REC
           RECORDING MODE F.
       01 TRAN-HAND-REC.
           05 TRAN-HAND-POWER          PIC 9(1).
           05 TRAN-HAND-FILL-1         PIC X(1) VALUE " ".
           05 TRAN-HAND-ID             PIC X(5).
           05 TRAN-HAND-FILL-2         PIC X(1) VALUE " ".
           05 TRAN-HAND-SCORE          PIC 9(4).

      * temporary work file for sorting
       SD WORKFILE
           RECORD CONTAINS 11 CHARACTERS
           DATA RECORD WORK-HAND-REC
           RECORDING MODE F.
       01 WORK-HAND-REC.
           05 WORK-HAND-POWER          PIC 9(1).
           05 WORK-HAND-ID             PIC X(5).
           05 WORK-HAND-SCORE          PIC 9(4).

       WORKING-STORAGE SECTION.
       01 EOF.
      *      Reading input related EOF
          05 EOF-IN                    PIC X(01) VALUE 'N'.
             88 EOF-IN-TRUE            VALUE 'Y'.
             88 EOF-IN-FALSE           VALUE 'N'.
      *      Saving transformed data EOF
          05 EOF-TRAN                  PIC X(01) VALUE 'N'.
             88 EOF-TRAN-TRUE          VALUE 'Y'.
             88 EOF-TRAN-FALSE         VALUE 'N'.
      * HAND processing
       01 TEMP_ID.
           02 TA                       PIC X(1)
                                       OCCURS 5 TIMES.
       01 BUBBLE.
           05 BUBBLE_I                 PIC 9(1).
           05 BUBBLE_J                 PIC 9(1).
           05 BUBBLE_SWAP              PIC X(1).
       01 RANK                         PIC 9(16) VALUE 1.
       01 WINNINGS                     PIC 9(16) VALUE 0.

       PROCEDURE DIVISION.
           DISPLAY 'For the sand!'.
      D    DISPLAY 'Debug mode is ON'.

      * Reading and displaying all the records
      D    OPEN INPUT CARDS. 
      D    SET  EOF-IN-FALSE           TO  TRUE.
      D    PERFORM UNTIL EOF-IN-TRUE
      D         READ CARDS 
      D                  AT END SET EOF-IN-TRUE TO TRUE
      D             NOT AT END DISPLAY
      D                 "'", IN-HAND-ID, "' '", IN-HAND-SCORE, "'"
      D         END-READ
      D    END-PERFORM.
      D    CLOSE CARDS. 

           SORT WORKFILE
             ON DESCENDING KEY WORK-HAND-POWER, WORK-HAND-ID
             INPUT PROCEDURE IS map-input-file
             OUTPUT PROCEDURE IS write-transformed-file.

      *    # Part 01: 249638405
           DISPLAY "Part 01: ", WINNINGS

           STOP RUN.



      * Reads the input file and transforms card IDs
        map-input-file.
           SET  EOF-IN-FALSE      TO TRUE
           OPEN INPUT CARDS.

           PERFORM UNTIL EOF-IN-TRUE
              READ CARDS
                  AT END SET EOF-IN-TRUE TO TRUE 
                  NOT AT END PERFORM
      D               DISPLAY "Reading: ", IN-HAND-REC
                      PERFORM map-cards
      D               DISPLAY "Release: ", WORK-HAND-REC
                      RELEASE WORK-HAND-REC
                  END-PERFORM
              END-READ

           END-PERFORM

           CLOSE CARDS.



       map-cards.
        INSPECT IN-HAND-ID CONVERTING
        "AKQJT98765432" TO
        "abcdefghijklm".

        MOVE IN-HAND-ID TO TEMP_ID
        MOVE 0 TO BUBBLE_I, BUBBLE_J
        PERFORM VARYING BUBBLE_I FROM 1 BY 1 UNTIL BUBBLE_I > 5
        PERFORM VARYING BUBBLE_J FROM BUBBLE_I BY 1 UNTIL BUBBLE_J > 5
           IF TA(BUBBLE_I) < TA(BUBBLE_J)
               MOVE TA(BUBBLE_J) TO BUBBLE_SWAP
               MOVE TA(BUBBLE_I) TO TA(BUBBLE_J)
               MOVE BUBBLE_SWAP  TO TA(BUBBLE_I)
           END-IF
        END-PERFORM
        END-PERFORM

       EVALUATE TRUE 
      * 5 same
        WHEN TA(1)=TA(2) AND TA(2)=TA(3) AND TA(3)=TA(4) AND TA(4)=TA(5)
               MOVE 0 TO WORK-HAND-POWER

      * 4 same
        WHEN TA(1)=TA(2) AND TA(2)=TA(3) AND TA(3)=TA(4)                
        WHEN                 TA(2)=TA(3) AND TA(3)=TA(4) AND TA(4)=TA(5)
               MOVE 1 TO WORK-HAND-POWER

        WHEN TA(1)=TA(2)                 AND TA(3)=TA(4) AND TA(4)=TA(5)
        WHEN TA(1)=TA(2) AND TA(2)=TA(3)                 AND TA(4)=TA(5)
               MOVE 2 TO WORK-HAND-POWER

      * 3 same
        WHEN TA(1)=TA(2) AND TA(2)=TA(3)                                
        WHEN                 TA(2)=TA(3) AND TA(3)=TA(4)                
        WHEN                                 TA(3)=TA(4) AND TA(4)=TA(5)
               MOVE 3 TO WORK-HAND-POWER

      * 2 pairs
        WHEN TA(1)=TA(2)                 AND TA(3)=TA(4)                
        WHEN TA(1)=TA(2)                                 AND TA(4)=TA(5)
        WHEN                 TA(2)=TA(3)                 AND TA(4)=TA(5)
               MOVE 4 TO WORK-HAND-POWER

      * 2 same
        WHEN TA(1)=TA(2)                                                
        WHEN                 TA(2)=TA(3)                                
        WHEN                                 TA(3)=TA(4)               
        WHEN                                                 TA(4)=TA(5)
               MOVE 5 TO WORK-HAND-POWER

      * None same
        WHEN OTHER
               MOVE 6 TO WORK-HAND-POWER
       END-EVALUATE

        MOVE IN-HAND-ID      TO WORK-HAND-ID.
        MOVE IN-HAND-SCORE   TO WORK-HAND-SCORE.



        write-transformed-file.
           OPEN OUTPUT TRANSFORMED.
           PERFORM UNTIL EOF-TRAN-TRUE
                   RETURN WORKFILE
                          AT END SET EOF-TRAN-TRUE   TO TRUE   
                      NOT AT END PERFORM write-work-to-transformed
                   END-RETURN 
           END-PERFORM.

           CLOSE TRANSFORMED. 



       write-work-to-transformed.
           MOVE WORK-HAND-POWER   TO TRAN-HAND-POWER
           MOVE WORK-HAND-ID      TO TRAN-HAND-ID
           MOVE WORK-HAND-SCORE   TO TRAN-HAND-SCORE
           MOVE " " TO TRAN-HAND-FILL-1
           MOVE " " TO TRAN-HAND-FILL-2

      D    DISPLAY WINNINGS, " ", RANK, " ", TRAN-HAND-SCORE
           COMPUTE WINNINGS =
           WINNINGS + RANK * FUNCTION NUMVAL(TRAN-HAND-SCORE)
           COMPUTE RANK = RANK + 1
      D DISPLAY TRAN-HAND-POWER, TRAN-HAND-ID, " ", TRAN-HAND-SCORE
      D DISPLAY "Write: '", TRAN-HAND-REC, "'"
           WRITE TRAN-HAND-REC
