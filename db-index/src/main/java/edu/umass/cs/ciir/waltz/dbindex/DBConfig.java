package edu.umass.cs.ciir.waltz.dbindex;

/**
 * @author jfoley
 */
public class DBConfig {
  private final String jdbcURL;
  private final String password;
  private final String user;

  public DBConfig(String jdbcURL, String user, String pass) {
    this.jdbcURL = jdbcURL;
    this.user = user;
    this.password = pass;
  }
  public DBConfig(String jdbcURL) {
    this(jdbcURL, "user", "");
  }
  public String getJDBCURL() { return jdbcURL; }
  public String getUser() { return user; }
  public String getPassword() { return password; }
}
