package dataStructure;

public class InputClass {
	public String name;
	public String type;
	public String min;
	public String max;
	public String defaultValue;
	public boolean valid;
	
	public InputClass(String name, String type, String min, String max) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
	}
	
	public InputClass(String name, String type, boolean valid) {
		super();
		this.name = name;
		this.type = type;
		this.valid = valid;
	}
	
	/*public InputClass(String name, String type, String min, String max, boolean valid) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.valid = valid;
	}*/
	
	public InputClass(String name, String type, String min, String max, String defaultValue, boolean valid) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.defaultValue = defaultValue;
		this.valid = valid;
	}

}
