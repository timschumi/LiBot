package com.mz.libot.core.data.providers.impl;

import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.reflect.TypeToken;
import com.mz.libot.core.BotData;
import com.mz.libot.core.data.providers.SnowflakeProvider;
import com.mz.libot.core.data.providers.impl.MoneyProviders.AdditionalMoneyProvider.AdditionalData;

import net.dv8tion.jda.api.entities.User;

public class MoneyProviders {

	public static class MoneyProvider extends SnowflakeProvider<Long> {

		public static final long DEFAULT_AMOUNT = 50L;

		public MoneyProvider() {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {

				Thread.getAllStackTraces()
				    .keySet()
				    .stream()
				    .map(Thread::getName)
				    .filter(tn -> tn.contains("bettable=") && !tn.contains("bettable={money}"))
				    .forEach(tn -> {

					    String[] tns = tn.split("&&");
					    long userId = Long.parseLong(tns[3]);

					    Long balance = this.data.get(userId);
					    if (balance != null) {
						    this.data.put(userId, balance + Long.parseLong(tns[6].split("=")[1]));
					    }

				    });

				this.store(BotData.getProperties(), false);

			}));
		}

		private static final TypeToken<Map<Long, Long>> TYPE_TOKEN = new TypeToken<>() {};

		/**
		 * @param user
		 * @return user's balance
		 */
		public long getBalance(User user) {
			Long balance = this.data.get(user.getIdLong());

			if (balance == null) {
				setBalance(user, DEFAULT_AMOUNT);
				balance = DEFAULT_AMOUNT;
			}

			return balance;

		}

		/**
		 * Sets user's balance to a specific amount of money. If you try to set a value below
		 * 0, value of 0 will be set.
		 *
		 * @param user
		 * @param balance
		 */
		public void setBalance(User user, long balance) {
			long newBalance = balance;
			if (newBalance < 0)
				newBalance = 0;

			this.data.put(user.getIdLong(), newBalance);

			super.store(BotData.getProperties());
		}

		/**
		 * Adds money to user's balance.
		 *
		 * @param user
		 * @param amount
		 * @return user's new balance
		 */
		public long addMoney(User user, long amount) {
			this.data.put(user.getIdLong(), getBalance(user) + amount);

			super.store(BotData.getProperties());

			return getBalance(user);
		}

		/**
		 * Adds money to user's balance. If you try to set a value below 0, value of 0 will
		 * be set.
		 *
		 * @param user
		 * @param amount
		 * @return user's new balance
		 */
		public long retractMoney(User user, long amount) {
			this.data.put(user.getIdLong(), getBalance(user) - amount);

			super.store(BotData.getProperties());

			return getBalance(user);
		}

		@Override
		public TypeToken<Map<Long, Long>> getTypeToken() {
			return TYPE_TOKEN;
		}

		@Override
		protected Predicate<Long> getObsoleteFilter() {
			return null;
		}

		@Override
		public String getDataKey() {
			return "money";
		}

	}

	public static class AdditionalMoneyProvider extends SnowflakeProvider<AdditionalData> {

		public static class AdditionalData {

			private boolean leaderboard = true;

			public boolean isLeaderboard() {
				return this.leaderboard;
			}

			public AdditionalData setLeaderboard(boolean leaderboard) {
				this.leaderboard = leaderboard;

				return this;
			}

		}

		private static final Predicate<Long> FILTER = id -> BotData.getJDA().getUserById(id.longValue()) == null;

		/**
		 * @param user
		 * @return {@link AdditionalData} assigned to the user, or a new
		 *         {@link AdditionalData} if there is no {@link AdditionalData} assigned
		 */
		public AdditionalData getData(User user) {
			return this.data.getOrDefault(user.getIdLong(), new AdditionalData());
		}

		/**
		 * @param userId
		 * @return {@link AdditionalData} assigned to the user, or a new
		 *         {@link AdditionalData} if there is no {@link AdditionalData} assigned
		 */
		public AdditionalData getData(long userId) {
			return this.data.getOrDefault(userId, new AdditionalData());
		}

		/**
		 * Sets {@link AdditionalData} for a user
		 *
		 * @param user
		 * @param data
		 */
		public void setData(User user, AdditionalData data) {
			this.data.put(user.getIdLong(), data);

			super.store(BotData.getProperties());
		}

		@Override
		public TypeToken<Map<Long, AdditionalData>> getTypeToken() {
			return new TypeToken<>() {};
		}

		@Override
		protected Predicate<Long> getObsoleteFilter() {
			return FILTER;
		}

		@Override
		public String getDataKey() {
			return "additionalmoney";
		}

	}

	private MoneyProviders() {}

}
