package e.administrateur.cardioproject;


class Server {//CAN DISAPEAR WITH PERSISTANCE
    private String ipAddress;
    private static final Server ourInstance = new Server();

    String getIpAddress() {
        return ipAddress;
    }

    void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    static Server getInstance() {
        return ourInstance;
    }

    private Server() {
    }
}
