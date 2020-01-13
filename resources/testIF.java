class testIF {
	int even;
	int size;
	int ret;
	
	public void func(int patch, int posX, int posY) {
		int tempA;
		int tempB;
		if ((patch < 2) && (patch >= 1))
			tempA = posY - 2;
		else if (patch == 2) {
			tempA = posX - 2;
			tempA = tempA + 1;
		}
		else {
			tempA = posX + 2;
		}
		tempB = tempA - posX;
		this.even = tempA;
		this.size = tempB;
		this.ret = tempA + tempB + 2;
		return this.ret;
	}
}