/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

/**
 * Users packet extension.
 *
 * @author Sebastien Vincent
 */
public class UsersPacketExtension extends AbstractPacketExtension
{
	/**
	 * The name of the element that contains the users data.
	 */
	public static final String ELEMENT_NAME = "users";

	/**
	 * The namespace that users belongs to.
	 */
	public static final String NAMESPACE = null;

	/**
	 * Entity attribute name.
	 */
	public static final String STATE_ATTR_NAME = "state";

	/**
	 * Constructor.
	 */
	public UsersPacketExtension()
	{
		super(ELEMENT_NAME, NAMESPACE);
	}
}
