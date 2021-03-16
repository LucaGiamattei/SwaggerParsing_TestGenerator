package dataStructure;

public class InputClass {
	public String name;
	public String type;
	public String min;
	public String max;
	public boolean valid;
	
	public InputClass(String name, String type, String min, String max) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
	}
	
	public InputClass(String name, String type, String min, String max, boolean valid) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.valid = valid;
	}

}
