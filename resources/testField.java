class test {
	
	public void func(Tmp tmp) {
		Tmp tmpA = tmp;
		Tmp tmpB = tmp;
		int tempA = tmpA.even - 2;
		int tempB = tempA - tmpB.even;
		tmpA.even = tempA;
		tmpA.size = tempB;
		tmpB.even = tempA;
		tmpB = tmpA;
		tmpB.size = tempB;
		int ret = tmpB.size + tmpB.even + 2;
		return ret;
	}
	
	class Tmp {
		public int even;
		public int size;
	}
}