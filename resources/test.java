class test {
	int even;
	int size;
	int ret;
	
	public void func(int posX, int posY) {
		int tempA = posX - 2;
		int tempB = tempA - posX;
		this.even = tempA;
		this.size = tempB;
		this.ret = tempA + tempB + 2;
		return this.ret;
	}
}