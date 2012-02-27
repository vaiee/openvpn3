// TESTING_ONLY

public class Client implements OpenVPNClientThread.EventReceiver {
    private OpenVPNClientThread client_thread;

    public static class ConfigError extends Exception {
	public ConfigError(String msg) { super(msg); }
    }

    public static class CredsUnspecifiedError extends Exception {
	public CredsUnspecifiedError(String msg) { super(msg); }
    }

    // Load OpenVPN core (implements OpenVPNClientBase) from shared library 
    static {
	System.loadLibrary("ovpncli");
    }

    public Client(String config_text, String username, String password) throws ConfigError, CredsUnspecifiedError {
	// init client implementation object
	client_thread = new OpenVPNClientThread();

	// load/eval config
	Config config = new Config();
	config.setContent(config_text);
	EvalConfig ec = client_thread.eval_config(config);
	if (ec.getError())
	    throw new ConfigError("OpenVPN config file parse error: " + ec.getMessage());

	// handle creds
	ProvideCreds creds = new ProvideCreds();
	if (!ec.getAutologin())
	    {
		if (username.length() > 0)
		    {
			creds.setUsername(username);
			creds.setPassword(password);
			creds.setReplacePasswordWithSessionID(true);
		    }
		else
		    throw new CredsUnspecifiedError("OpenVPN config file requires username/password but none provided");
	    }
	client_thread.provide_creds(creds);
    }

    public void connect() {
	// connect
	client_thread.connect(this);

	// wait for worker thread to exit
	client_thread.wait_thread();
    }

    public void stop() {
	client_thread.stop();
    }

    public void show_stats() {
	int n = client_thread.stats_n();
	for (int i = 0; i < n; ++i)
	    {
		String name = client_thread.stats_name(i);
		long value = client_thread.stats_value(i);
		if (value > 0)
		    System.out.format("STAT %s=%s%n", name, value);
	    }
    }

    @Override
    public void event(Event event) {
	boolean error = event.getError();
	String name = event.getName();
	String info = event.getInfo();
	System.out.format("EVENT: err=%b name=%s info='%s'%n", error, name, info);
    }

    @Override
    public void log(LogInfo loginfo) {
	String text = loginfo.getText();
	System.out.format("LOG: %s", text);
    }

    @Override
    public void done(Status status) {
	System.out.format("DONE Status: err=%b msg='%s'%n", status.getError(), status.getMessage());
    }

    @Override
    public boolean socket_protect(int socket)
    {
	return false;
    }

    @Override
    public OpenVPNClientThread.TunBuilder tun_builder_new()
    {
	return null;
    }
 }
