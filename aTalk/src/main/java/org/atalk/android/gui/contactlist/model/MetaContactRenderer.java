/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.android.gui.contactlist.model;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSet;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetExtendedAuthorizations;
import net.java.sip.communicator.service.protocol.OperationSetFileTransfer;
import net.java.sip.communicator.service.protocol.OperationSetVideoTelephony;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.util.StatusUtil;

import org.atalk.android.aTalkApp;
import org.atalk.android.gui.AndroidGUIActivator;
import org.atalk.android.gui.chat.ChatSessionManager;
import org.atalk.android.gui.util.AndroidImageUtil;
import org.atalk.android.gui.util.DrawableCache;
import org.atalk.util.StringUtils;

import java.util.Iterator;

/**
 * Class used to obtain UI specific data for <tt>MetaContact</tt> instances.
 *
 * @author Pawel Domas
 */
public class MetaContactRenderer implements UIContactRenderer
{
	@Override
	public boolean isSelected(Object contactImpl)
	{
		return MetaContactListAdapter.isContactSelected((MetaContact) contactImpl);
	}

	@Override
	public String getDisplayName(Object contactImpl)
	{
		return ((MetaContact) contactImpl).getDisplayName();
	}

	@Override
	public String getStatusMessage(Object contactImpl)
	{
		MetaContact metaContact = (MetaContact) contactImpl;
		String displayDetails = getDisplayDetails(metaContact);
		return displayDetails != null ? displayDetails : "";
	}

	@Override
	public boolean isDisplayBold(Object contactImpl)
	{
		return ChatSessionManager.getActiveChat((MetaContact) contactImpl) != null;
	}

	@Override
	public Drawable getAvatarImage(Object contactImpl)
	{
		return getAvatarDrawable((MetaContact) contactImpl);
	}

	@Override
	public Drawable getStatusImage(Object contactImpl)
	{
		return getStatusDrawable((MetaContact) contactImpl);
	}

	@Override
	public boolean isShowVideoCallBtn(Object contactImpl)
	{
		return isShowButton((MetaContact) contactImpl, OperationSetVideoTelephony.class);
	}

	@Override
	public boolean isShowCallBtn(Object contactImpl)
	{
		return isShowButton((MetaContact) contactImpl, OperationSetBasicTelephony.class);
	}

	@Override
	public boolean isShowFileSendBtn(Object contactImpl)
	{
		return isShowButton((MetaContact) contactImpl, OperationSetFileTransfer.class);
	}

	@Override
	public String getDefaultAddress(Object contactImpl)
	{
		return ((MetaContact) contactImpl).getDefaultContact().getAddress();
	}

	/**
	 * Returns the display details for the underlying <tt>MetaContact</tt>.
	 *
	 * @param metaContact
	 *        the <tt>MetaContact</tt>, which details we're looking for
	 * @return the display details for the underlying <tt>MetaContact</tt>
	 */
	private static String getDisplayDetails(MetaContact metaContact)
	{
		boolean subscribed = false;
		String displayDetails = null;
		String subscriptionDetails = null;

		Iterator<Contact> protoContacts = metaContact.getContacts();
		while (protoContacts.hasNext()) {
			Contact protoContact = protoContacts.next();

			OperationSetExtendedAuthorizations authOpSet = protoContact.getProtocolProvider()
					.getOperationSet(OperationSetExtendedAuthorizations.class);

			if ((authOpSet != null)
				&& (authOpSet.getSubscriptionStatus(protoContact) != null)
				&& !authOpSet.getSubscriptionStatus(protoContact).equals(
					OperationSetExtendedAuthorizations.SubscriptionStatus.Subscribed)) {
				OperationSetExtendedAuthorizations.SubscriptionStatus status
						= authOpSet.getSubscriptionStatus(protoContact);

				if (status.equals(OperationSetExtendedAuthorizations.SubscriptionStatus.SubscriptionPending))
					subscriptionDetails = AndroidGUIActivator.getResourcesService().getI18NString(
						"service.gui.WAITING_AUTHORIZATION");
				else if (status.equals(OperationSetExtendedAuthorizations.SubscriptionStatus.NotSubscribed))
					subscriptionDetails = AndroidGUIActivator.getResourcesService().getI18NString(
						"service.gui.NOT_AUTHORIZED");
			}
			else if (!StringUtils.isNullOrEmpty(protoContact.getStatusMessage())) {
				displayDetails = protoContact.getStatusMessage();
				subscribed = true;
				break;
			}
			else {
				subscribed = true;
			}
		}

		if (StringUtils.isNullOrEmpty(displayDetails) && !subscribed
				&& !StringUtils.isNullOrEmpty(subscriptionDetails))
			displayDetails = subscriptionDetails;

		return displayDetails;
	}

	private static boolean isShowButton(MetaContact metaContact, Class<? extends OperationSet> opSetClass)
	{
		return metaContact.getDefaultContact(opSetClass) != null;
	}

	/**
	 * Returns the status <tt>Drawable</tt> for the given <tt>MetaContact</tt>.
	 *
	 * @param metaContact
	 *        the <tt>MetaContact</tt>, which status drawable we're looking for
	 * @return a <tt>BitmapDrawable</tt> object representing the status of the given <tt>MetaContact</tt>
	 */
	public static BitmapDrawable getAvatarDrawable(MetaContact metaContact)
	{
		return getCachedAvatarFromBytes(metaContact.getAvatar());
	}

	/**
	 * Returns avatar <tt>BitmapDrawable</tt> with rounded corners. Bitmap will be cached in app global drawable cache.
	 * 
	 * @param avatar
	 *        raw avatar image data.
	 * @return avatar <tt>BitmapDrawable</tt> with rounded corners
	 */
	public static BitmapDrawable getCachedAvatarFromBytes(byte[] avatar)
	{
		if (avatar == null)
			return null;

		String bmpKey = String.valueOf(avatar.hashCode());
		DrawableCache cache = aTalkApp.getImageCache();

		BitmapDrawable avatarImage = cache.getBitmapFromMemCache(bmpKey);
		if (avatarImage == null) {
			BitmapDrawable roundedAvatar = AndroidImageUtil.roundedDrawableFromBytes(avatar);
			if (roundedAvatar != null) {
				avatarImage = roundedAvatar;
				cache.cacheImage(bmpKey, avatarImage);
			}
		}
		return avatarImage;
	}

	/**
	 * Returns the status <tt>Drawable</tt> for the given <tt>MetaContact</tt>.
	 *
	 * @param metaContact
	 *        the <tt>MetaContact</tt>, which status drawable we're looking for
	 * @return a <tt>Drawable</tt> object representing the status of the given <tt>MetaContact</tt>
	 */
	public static Drawable getStatusDrawable(MetaContact metaContact)
	{
		byte[] statusImage = getStatusImage(metaContact);

		if (statusImage != null)
			return AndroidImageUtil.drawableFromBytes(statusImage);

		return null;
	}

	/**
	 * Returns the array of bytes representing the status image of the given <tt>MetaContact</tt>.
	 *
	 * @return the array of bytes representing the status image of the given <tt>MetaContact</tt>
	 */
	private static byte[] getStatusImage(MetaContact metaContact)
	{
		PresenceStatus status = null;
		Iterator<Contact> contactsIter = metaContact.getContacts();
		while (contactsIter.hasNext()) {
			Contact protoContact = contactsIter.next();
			PresenceStatus contactStatus = protoContact.getPresenceStatus();

			if (status == null)
				status = contactStatus;
			else
				status = (contactStatus.compareTo(status) > 0) ? contactStatus : status;
		}

		return StatusUtil.getContactStatusIcon(status);
	}
}
