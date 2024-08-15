
package jaime.server;

import java.util.List;

public interface SocketProcess {
    public boolean bind();
    public void listen(Session sesion);
    public void startBroadcast();
    public void response(String message);
    public boolean close();
}
