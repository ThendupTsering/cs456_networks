
Step 1: Compile all the files
    - Navigate to the directory containing the makefile (a1)
    - Run 'make run' to compile all the files

Step 2: Set up the serverGBN
    - Navigate to the out directory where the shell scripts and compiled files are located
    - Run './serverGBN.sh <n_port>' where you can specify a random number >= 1024 for a negotiation port

Step 3: Connect the client to the serverGBN and reverse the string
    - In another window/computer (repeat Step 1 if on another computer), navigate to the out directory.
    - Run './client.sh <server_address> <n_port> <req_code> <msg>'
    - <n_port> here should match the output that the serverGBN prints i.e. "SERVER_PORT=<n_port>"
    - You should receive back the msg you sent to the serverGBN but in reversed form.


Hostname:
    1st Machine: ubuntu1404-010.student.cs.uwaterloo.ca
    2nd Machine: ubuntu1404-004.student.cs.uwaterloo.ca

Make version:
    GNU Make 3.81
    Copyright (C) 2006  Free Software Foundation, Inc.
    This is free software; see the source for copying conditions.
    There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
    PARTICULAR PURPOSE.

    This program built for i386-apple-darwin11.3.0