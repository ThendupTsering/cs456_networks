
Step 1: Compile all the files
    - Navigate to the directory containing the makefile (a2)
    - Run 'make run' to compile all the files

Step 2: Set up the Network emulator (host 1)
    - Navigate to the out directory where all the java classes are located
    - Run 'nEmulator 9991 host2 9994 9993 host3 9992 1 0.2 0'

Step 3: Set up the Receiver (host 2)
    - In another window/computer (repeat Step 1 if on another computer), navigate to the out directory.
    - Run 'java receiver host1 9993 9994 <outFileName>'

Step 3: Connect the Sender to the Receiver through the emulator (host 3)
    - In another window/computer (repeat Step 1 if on another computer), navigate to the out directory.
    - Run 'java sender host1 9991 9992 <inFile>'
    - The data in inFile should begin transferring to the receiver


Hostname:
    1st Machine: ubuntu1404-004.student.cs.uwaterloo.ca (Emulator)
    2nd Machine: ubuntu1404-010.student.cs.uwaterloo.ca (Receiver)
    3rd Machine: ubuntu1404-010.student.cs.uwaterloo.ca (Sender)

Make version:
    GNU Make 3.81
    Copyright (C) 2006  Free Software Foundation, Inc.
    This is free software; see the source for copying conditions.
    There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
    PARTICULAR PURPOSE.

    This program built for i386-apple-darwin11.3.0