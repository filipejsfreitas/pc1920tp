import socket
import random
import sys


def login_client(i, sock):
    sock.sendall(bytes("2\n", 'ascii'))
    sock.recv(65536)

    sock.sendall(bytes("c" + str(i) + "\r\n", 'ascii'))
    sock.recv(65536)

    sock.sendall(bytes("c" + str(i) + "\r\n", 'ascii'))
    sock.recv(65536)


def register_client(i, sock):
    sock.sendall(bytes("1\n", 'ascii'))
    sock.recv(65536)

    sock.sendall(bytes("c" + str(i) + "\r\n", 'ascii'))
    sock.recv(65536)

    sock.sendall(bytes("c" + str(i) + "\r\n", 'ascii'))
    sock.recv(65536)

    sock.sendall(bytes("c" + str(i) + "\r\n", 'ascii'))
    sock.recv(65536)
    sock.recv(65536)
    sock.recv(65536)


if __name__ == "__main__":
    MIN = int(sys.argv[1])
    NUM_CLIENTS = 1000 + MIN

    MAX_REGISTERED_CLIENT = 3000

    random.seed(NUM_CLIENTS)

    socks = []

    for i in range(MIN, NUM_CLIENTS):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect(("127.0.0.1", 1234))

        socks.append(sock)

    print("Criados todos os sockets")
    

    for i in range(MIN, NUM_CLIENTS):
        sock = socks[i - MIN]

        sock.recv(65536)

        if i <= MAX_REGISTERED_CLIENT:
            login_client(i, sock)
        else:
            register_client(i, sock)
    
    print("Registados/Loginados todos os clientes")

    for i in range(MIN, NUM_CLIENTS):
        sock = socks[i - MIN]

        sock.sendall(bytes("150\r\n", 'ascii'))
        sock.recv(65536)
        sock.recv(65536)
        sock.recv(65536)
    
    print("Atualizadas todas as contagens")
    
    for i in range(MIN, NUM_CLIENTS):
        sock = socks[i - MIN]
        
        sock.close()

    print("Fechados todos os sockets")

    pass
