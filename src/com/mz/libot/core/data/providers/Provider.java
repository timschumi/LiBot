package com.mz.libot.core.data.providers;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.Constants;
import com.mz.libot.core.data.Data;
import com.mz.libot.core.data.properties.PropertyManager;
import com.mz.libot.core.data.properties.impl.FTPPropertyManager;

import net.dv8tion.jda.api.JDA;

public abstract class Provider<T> implements Data {

	private static final Logger LOG = LoggerFactory.getLogger(Provider.class);
	protected static final Gson GSON = Constants.GSON;

	protected T data;
	private final Type type;

	public Provider() {
		this.type = getTypeToken().getType();
	}

	/**
	 * @return this provider's data
	 */
	public T getData() {
		return this.data;
	}

	/**
	 * @return the data key that will be used when loading & storing values
	 */
	public abstract String getDataKey();

	@Override
	public final void load(PropertyManager pm) {
		try {
			String json = pm.getProperty(this.getDataKey());

			if (json == null) {
				this.data = getDefaultData();
				return;
			}
			// In case the data itself is null (there is no data attached to that property
			// in the given PropertyManager)

			T data = constructData(json);

			if (data == null) {
				this.data = getDefaultData();
				return;
			}
			// In case the constructData(String) returns null

			this.data = data;

		} catch (Throwable t) {
			this.data = getDefaultData();

			onLoadFail(t);
		}

		onDataLoaded(pm);
	}

	@Override
	public final void store(PropertyManager pm) {
		try {
			pm.setProperty(getDataKey(), constructJson());

		} catch (Throwable t) {
			onStoreFail(t);
		}
	}

	/**
	 * Stores data of that class into the provided PropertyManager.
	 *
	 * @param pm
	 *            PropertyManager
	 * @param async
	 *            whether to apply async storing strategy, where possible
	 */
	public final void store(PropertyManager pm, boolean async) {
		try {
			if (pm instanceof FTPPropertyManager) {
				FTPPropertyManager ftppm = (FTPPropertyManager) pm;
				ftppm.setProperty(getDataKey(), constructJson(), async);
			} else {
				pm.setProperty(getDataKey(), constructJson());
			}

		} catch (Throwable t) {
			onStoreFail(t);
		}
	}

	/**
	 * @param json
	 *            JSON to parse
	 * @return data constructed out of the given JSON
	 * @throws JsonParseException
	 */
	protected T constructData(String json) {
		return GSON.<T>fromJson(json, this.type);
	}

	/**
	 * @return JSON constructed out of current data
	 */
	protected String constructJson() {
		return GSON.toJson(this.data);
	}

	/**
	 * @return returns the default (empty) data in case an exception is thrown when
	 *         trying to load the actual data
	 */
	protected T getDefaultData() {
		try {
			return constructData("{}");
		} catch (JsonParseException e) {
			return null;
		}
	}

	/**
	 * Will be called in case storing the data to the given PropertyManager fails in
	 * {@link this#store(PropertyManager)}
	 *
	 * @param t
	 */
	protected void onStoreFail(Throwable t) {
		LOG.error("Failed to store " + this.getClass().getSimpleName() + "; " + t.getMessage());
	}

	/**
	 * Will be called in case loading of the data from the PropertyManager fails in
	 * {@link this#load(PropertyManager)}.
	 *
	 * @param t
	 */
	protected void onLoadFail(Throwable t) {
		LOG.error("Failed to load " + this.getClass().getSimpleName() + "'s data; " + t.toString());
	}

	/**
	 * Will be called at the end of {@link #load(PropertyManager)}.
	 *
	 * @param pm
	 */
	@SuppressWarnings("unused")
	protected void onDataLoaded(PropertyManager pm) {}

	/**
	 * Cleans unused data of the provider. If the provider does not explicitly implement
	 * it, this won't do anything at all.
	 *
	 * @param jda
	 *            the JDA instance that will be used when cleaning
	 * @return a number of unused elements that were removed, by convention returns
	 *         {@code 0} if provider does not store data in separate elements, if the
	 *         provider does not support cleaning or if no obsolete elements were found
	 */
	@SuppressWarnings("unused")
	public int clean(JDA jda) {
		return 0;
	}

	public abstract TypeToken<T> getTypeToken();

}
