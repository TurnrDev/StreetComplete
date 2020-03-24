package de.westnordost.streetcomplete.data.user;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.data.user.achievements.Achievement;
import de.westnordost.streetcomplete.data.user.achievements.AchievementGiver;
import de.westnordost.streetcomplete.data.user.achievements.Link;
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsDao;
import de.westnordost.streetcomplete.data.user.achievements.UserLinksDao;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

@Module public class UserModule
{
	public static final String STATISTICS_BACKEND_URL = "https://www.westnordost.de/stats/";

	private static final String BASE_OAUTH_URL = "https://www.openstreetmap.org/oauth/";

	private static final String CONSUMER_KEY = "L3JyJMjVk6g5atwACVySRWgmnrkBAH7u0U18ALO7";
	private static final String CONSUMER_SECRET = "uNjPaXZw15CPHdCSeMzttRm20tyFGaBPO7jHt52c";

	private static final String CALLBACK_SCHEME = "streetcomplete";
	private static final String CALLBACK_HOST = "oauth";

	@Provides public static StatisticsDownloader statisticsDownloader()
	{
		return new StatisticsDownloader(STATISTICS_BACKEND_URL);
	}

	@Provides public static OAuthStore oAuthStore(SharedPreferences prefs)
	{
		return new OAuthStore(prefs, UserModule::defaultOAuthConsumer);
	}

	@Provides public static OAuthProvider oAuthProvider()
	{
		return new DefaultOAuthProvider(
			BASE_OAUTH_URL + "request_token",
			BASE_OAUTH_URL + "access_token",
			BASE_OAUTH_URL + "authorize");
	}

	@Provides public static OAuthConsumer defaultOAuthConsumer()
	{
		return new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	}

	@Provides @Named("OAuthCallbackScheme") public static String oAuthCallbackScheme()
	{
		return CALLBACK_SCHEME;
	}

	@Provides @Named("OAuthCallbackHost") public static String oAuthCallbackHost()
	{
		return CALLBACK_HOST;
	}

	@Provides @Singleton public static UserController userController(
		UserDao userDao, UserStore userStore, AchievementGiver achievementGiver,
		OAuthStore oAuthStore, UserAchievementsDao userAchievementsDao, UserLinksDao userLinksDao,
		@Named("Achievements") List<Achievement> achievements, @Named("Links") List<Link> links,
		Context context, StatisticsDownloader statisticsDownloader,
		QuestStatisticsDao statisticsDao, OsmConnection osmConnection
	)
	{
		return new UserController(
			userDao, oAuthStore, userStore, achievementGiver, userAchievementsDao, userLinksDao,
			achievements, links, OsmModule.getAvatarsCacheDirectory(context), statisticsDownloader,
			statisticsDao, osmConnection);
	}
}