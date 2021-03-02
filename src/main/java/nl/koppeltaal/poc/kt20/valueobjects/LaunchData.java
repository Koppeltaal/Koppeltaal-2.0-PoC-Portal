package nl.koppeltaal.poc.kt20.valueobjects;

/**
 *
 */
public class LaunchData {
	String url;
	String token;
	boolean redirect = false;

	public LaunchData(String url, String token, boolean redirect) {
		this.url = url;
		this.token = token;
		this.redirect = redirect;
	}

	public LaunchData(String url, String token) {
		this.url = url;
		this.token = token;
	}

	public LaunchData(){

	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isRedirect() {
		return redirect;
	}

	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}
}
