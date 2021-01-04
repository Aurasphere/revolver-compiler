package co.aurasphere.revolver.test.breaking;

import javax.inject.Singleton;

public class ConstructorCircularDependencyBreakingTest {

	@Singleton
	public class ConstructorComponentOne {

		private ConstructorComponentTwo constructorComponentTwo;

		public ConstructorComponentOne(ConstructorComponentTwo constructorComponentTwo) {
			this.constructorComponentTwo = constructorComponentTwo;
		}

		public ConstructorComponentTwo getConstructorComponentTwo() {
			return constructorComponentTwo;
		}

		public void setConstructorComponentTwo(ConstructorComponentTwo constructorComponentTwo) {
			this.constructorComponentTwo = constructorComponentTwo;
		}
	}

	@Singleton
	public class ConstructorComponentTwo {

		private ConstructorComponentThree constructorComponentThree;

		public ConstructorComponentTwo(ConstructorComponentThree constructorComponentThree) {
			this.constructorComponentThree = constructorComponentThree;
		}

		public ConstructorComponentThree getConstructorComponentThree() {
			return constructorComponentThree;
		}

		public void setConstructorComponentThree(ConstructorComponentThree constructorComponentThree) {
			this.constructorComponentThree = constructorComponentThree;
		}
	}
	
	@Singleton
	public class ConstructorComponentThree {

		private ConstructorComponentOne constructorComponentOne;

		public ConstructorComponentThree(ConstructorComponentOne constructorComponentOne) {
			this.constructorComponentOne = constructorComponentOne;
		}

		public ConstructorComponentOne getConstructorComponentOne() {
			return constructorComponentOne;
		}

		public void setConstructorComponentOne(ConstructorComponentOne constructorComponentOne) {
			this.constructorComponentOne = constructorComponentOne;
		}
	}
}