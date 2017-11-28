package be.nikiroo.utils.serial.server;

import java.io.IOException;
import java.net.Socket;

import be.nikiroo.utils.Version;

/**
 * Class used for the server basic handling.
 * <p>
 * It represents a single action: a server is expected to execute one action for
 * each client action.
 * 
 * @author niki
 */
public class ConnectActionServerObject extends ConnectActionServer {
	/**
	 * Create a new {@link ConnectActionServerObject} with the current
	 * application version (see {@link Version#getCurrentVersion()}) as the
	 * server version.
	 * 
	 * @param s
	 *            the socket to bind to
	 */
	public ConnectActionServerObject(Socket s) {
		super(s);
	}

	/**
	 * Create a new {@link ConnectActionServerObject}.
	 * 
	 * @param s
	 *            the socket to bind to
	 * @param version
	 *            the server version
	 */
	public ConnectActionServerObject(Socket s, Version version) {
		super(s, version);
	}

	/**
	 * Serialise and send the given object to the client.
	 * 
	 * @param data
	 *            the data to send
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws NoSuchFieldException
	 *             if the serialised data contains information about a field
	 *             which does actually not exist in the class we know of
	 * @throws NoSuchMethodException
	 *             if a class described in the serialised data cannot be created
	 *             because it is not compatible with this code
	 * @throws ClassNotFoundException
	 *             if a class described in the serialised data cannot be found
	 */
	public void send(Object data) throws IOException, NoSuchFieldException,
			NoSuchMethodException, ClassNotFoundException {
		action.sendObject(data);
	}

	/**
	 * (Flush the data to the client if needed and) retrieve its answer.
	 * 
	 * @return the deserialised answer (which can actually be NULL)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * @throws NoSuchFieldException
	 *             if the serialised data contains information about a field
	 *             which does actually not exist in the class we know of
	 * @throws NoSuchMethodException
	 *             if a class described in the serialised data cannot be created
	 *             because it is not compatible with this code
	 * @throws ClassNotFoundException
	 *             if a class described in the serialised data cannot be found
	 * @throws java.lang.NullPointerException
	 *             if the counter part has no data to send
	 */
	public Object rec() throws NoSuchFieldException, NoSuchMethodException,
			ClassNotFoundException, IOException, java.lang.NullPointerException {
		return action.recObject();
	}
}
