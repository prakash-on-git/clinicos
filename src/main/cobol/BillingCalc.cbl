      *>-----------------------------------------------------------
      *> BILLING-CALC
      *>
      *> Computes discount, tax, and total for one invoice using
      *> COBOL fixed-point decimal arithmetic (COMP-3-equivalent
      *> DISPLAY numerics) instead of IEEE binary floating point.
      *> Financial totals never accumulate the rounding error that
      *> binary floats introduce on values like 0.10 or 19.99 — the
      *> same reason production banking cores still run COBOL today.
      *>
      *> Protocol: one fixed-width line in on STDIN, one fixed-width
      *> line out on STDOUT. No headers, no delimiters — every field
      *> is unsigned digits with an implied 2-decimal-place scale
      *> (e.g. "000150050" = 1500.50). The Java caller (CobolBilling
      *> Calculator) formats input and parses output to this exact
      *> layout.
      *>
      *> Input  (28 chars): SUBTOTAL(9) DISCOUNT-PCT(5) DISCOUNT-AMT(9) TAX-PCT(5)
      *> Output (27 chars): DISCOUNT(9) TAX(9) TOTAL(9)
      *>-----------------------------------------------------------
       IDENTIFICATION DIVISION.
       PROGRAM-ID. BILLING-CALC.

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       01 WS-INPUT-LINE.
          05 WS-SUBTOTAL          PIC 9(7)V99.
          05 WS-DISCOUNT-PCT      PIC 9(3)V99.
          05 WS-DISCOUNT-AMT      PIC 9(7)V99.
          05 WS-TAX-PCT           PIC 9(3)V99.

       01 WS-DISCOUNT-CALC        PIC 9(7)V99.
       01 WS-TAXABLE-AMOUNT       PIC 9(7)V99.
       01 WS-TAX-CALC             PIC 9(7)V99.
       01 WS-TOTAL                PIC 9(7)V99.

       01 WS-OUTPUT-LINE.
          05 WS-OUT-DISCOUNT      PIC 9(7)V99.
          05 WS-OUT-TAX           PIC 9(7)V99.
          05 WS-OUT-TOTAL         PIC 9(7)V99.

       PROCEDURE DIVISION.
       MAIN-LOGIC.
           ACCEPT WS-INPUT-LINE FROM CONSOLE.

           IF WS-DISCOUNT-PCT > 0
               COMPUTE WS-DISCOUNT-CALC ROUNDED =
                   WS-SUBTOTAL * WS-DISCOUNT-PCT / 100
           ELSE
               MOVE WS-DISCOUNT-AMT TO WS-DISCOUNT-CALC
           END-IF.

           COMPUTE WS-TAXABLE-AMOUNT = WS-SUBTOTAL - WS-DISCOUNT-CALC.

           COMPUTE WS-TAX-CALC ROUNDED =
               WS-TAXABLE-AMOUNT * WS-TAX-PCT / 100.

           COMPUTE WS-TOTAL = WS-TAXABLE-AMOUNT + WS-TAX-CALC.

           MOVE WS-DISCOUNT-CALC TO WS-OUT-DISCOUNT.
           MOVE WS-TAX-CALC      TO WS-OUT-TAX.
           MOVE WS-TOTAL         TO WS-OUT-TOTAL.

           DISPLAY WS-OUTPUT-LINE.

           STOP RUN.
