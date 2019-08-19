package net.benjaminurquhart.codinbot.api;

public class APIException extends RuntimeException {

	private static final long serialVersionUID = -6893142342372959070L;
	private Exception e;
	
	public APIException(Exception e) {
		super(e.getMessage());
		this.e = e;
	}
	public String toString() {
		return this.getClass().getName() + ": Ran into an exception when fetching data from the API!\n"+e;
	}
	public Throwable getCause() {
		return e;
	}
}
