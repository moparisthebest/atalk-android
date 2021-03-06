/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.android.gui.account;

import android.app.AlertDialog;
import android.content.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.account.AccountUtils;

import org.atalk.android.R;

import static net.java.sip.communicator.plugin.otr.OtrActivator.configService;

/**
 * Helper class that produces "remove account dialog". It asks the user for account removal
 * confirmation and finally removes the account. Interface <tt>OnAccountRemovedListener</tt> is
 * used to notify about account removal which will not be fired if the user cancels the dialog.
 *
 * @author Pawel Domas
 * @author Eng Chong Meng
 */
public class RemoveAccountDialog
{
	public static AlertDialog create(Context ctx, final Account account,
			final OnAccountRemovedListener listener)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		return alert.setTitle(R.string.service_gui_REMOVE_ACCOUNT)
				.setMessage(ctx.getString(R.string.service_gui_REMOVE_ACCOUNT_MESSAGE,
						account.getAccountID().getDisplayName()))
				.setPositiveButton(R.string.service_gui_YES, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						onRemoveClicked(dialog, account, listener);
					}
				}).setNegativeButton(R.string.service_gui_NO, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				}).create();
	}

	private static void onRemoveClicked(final DialogInterface dialog, final Account account,
			final OnAccountRemovedListener l)
	{
		// Fix "network on main thread"
		final Thread removeAccountThread = new Thread()
		{
			@Override
			public void run()
			{
				// purge persistent storage must happen before removeAccount action
				AccountsListActivity.removeAccountPersistentStore(account);
				removeAccount(account);
			}
		};
		removeAccountThread.start();
		try {
			// Simply block UI thread as it shouldn't take too long to uninstall
			removeAccountThread.join();
			// Notify about results
			l.onAccountRemoved(account);
			dialog.dismiss();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes given <tt>AccountID</tt> from the system.
	 * Note: accountUuid without suffix as propertyName will remove the account
	 *
	 * @param account
	 * 		the account that will be uninstalled from the system.
	 */
	private static void removeAccount(Account account)
	{
		AccountID accountID = account.getAccountID();
		ProtocolProviderFactory providerFactory
				= AccountUtils.getProtocolProviderFactory(accountID.getProtocolName());
		String accountUuid = accountID.getAccountUuid();
		configService.setProperty(accountUuid, null);

//		ConfigurationService configService = AndroidGUIActivator.getConfigurationService();
//		String prefix = "net.java.sip.communicator.impl.gui.accounts";
//		List<String> accounts = configService.getPropertyNamesByPrefix(prefix, true);
//		for (String accountRootPropName : accounts) {
//			String accountUID = configService.getString(accountRootPropName);
//
//			if (accountUID.equals(accountID.getAccountUniqueID())) {
//				configService.setProperty(accountRootPropName, null);
//				break;
//			}
//		}

		boolean isUninstalled = providerFactory.uninstallAccount(accountID);
		if (!isUninstalled)
			throw new RuntimeException("Failed to uninstall account");
	}

	/**
	 * Interfaces used to notify about account removal which happens after the user confirms the
	 * action.
	 */
	interface OnAccountRemovedListener
	{
		/**
		 * Fired after <tt>Account</tt> is removed from the system which happens after user
		 * confirms the action. Will not be fired when user dismisses the dialog.
		 *
		 * @param account
		 * 		removed <tt>Account</tt>.
		 */
		void onAccountRemoved(Account account);
	}

}
