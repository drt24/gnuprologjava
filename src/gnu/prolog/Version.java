/* GNU Prolog for Java
 * Copyright (C) 2009       Michiel Hendriks
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text of license can be also found
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog;

import java.io.InputStream;
import java.util.Properties;

/**
 * Version information for gnuprolog
 */
public final class Version
{
	/**
	 * Utility class: hide constructor
	 */
	private Version()
	{}

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
	 * @return 10000 * Major + 100 * Minor + Revision
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
	private final static class VersionInternal
	{
		private VersionInternal()
		{}

		static int major;
		static int minor;
		static int revision;
		static String type = null;

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
