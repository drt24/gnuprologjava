package gnu.prolog;

import java.io.InputStream;
import java.util.Properties;

/**
 * Version information for gnuprolog
 */
public class Version
{
	/**
	 * @return the version string
	 */
	public static String getVersion()
	{
		if (VersionInternal.type == null || VersionInternal.type.length() == 0)
		{
			return String.format("%d.%d.%d", VersionInternal.major, VersionInternal.minor, VersionInternal.revision);
		}
		else
		{
			return String.format("%d.%d.%d %s", VersionInternal.major, VersionInternal.minor, VersionInternal.revision,
					VersionInternal.type);
		}
	}

	/**
	 * @return the major version number
	 */
	public static int getMajor()
	{
		return VersionInternal.major;
	}

	/**
	 * @return the minor version number
	 */
	public static int getMinor()
	{
		return VersionInternal.minor;
	}

	/**
	 * @return the revision number
	 */
	public static int getRevision()
	{
		return VersionInternal.revision;
	}

	/**
	 * @return 10000 × Major + 100 × Minor + Revision
	 */
	public static int intEncoded()
	{
		return getMajor() * 10000 + getMinor() * 100 + getRevision();
	}

	/**
	 * @return the release type: alpha, beta, rc, or null for stable releases
	 */
	public static String getReleaseType()
	{
		return VersionInternal.type;
	}

	/**
	 * Internal class responsible for loading
	 */
	private static class VersionInternal
	{

		private static int major;
		private static int minor;
		private static int revision;
		private static String type = null;

		static
		{
			Properties prop = new Properties();
			InputStream is = VersionInternal.class.getResourceAsStream("version.properties");
			if (is != null)
			{
				try
				{
					prop.load(is);
					final String base = "gnu.prolog";
					major = Integer.parseInt(prop.getProperty(base + ".version.major", Integer.toString(major)));
					minor = Integer.parseInt(prop.getProperty(base + ".version.minor", Integer.toString(minor)));
					revision = Integer.parseInt(prop.getProperty(base + ".version.revision", Integer.toString(revision)));
					type = prop.getProperty(base + ".version.type", type);
					is.close();
				}
				catch (Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
