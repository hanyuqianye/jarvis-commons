package org.divxdede.commons;

/**
 * This class describe a Condition that can be satisfied or not.<br>
 * The method {@link #isTrue()} evaluate this condition at the instant time.
 * <p>
 * Conditions can be assembled to form a more complex condition using basic logics operators like:
 * <ul>
 *   <li>{@link #and(Condition)} for an AND operation between 2 conditions</li>
 *   <li>{@link #or(Condition)} for an OR operation between 2 conditions</li>
 *   <li>{@link #xor(Condition)} for an XOR operation between 2 conditions</li>
 *   <li>{@link #not()} for a NOT unary operator on this condition</li>
 * </ul>
 * <p>
 * Subclassing a Condition must implements {@link #isTrue()}
 * 
 * @author André Sébastien (divxdede)
 * @since 0.2
 */
public abstract class Condition {

    /** Evaluate this condition.
     *  @return <code>true</code> if this logic condition is satisfied, <code>false</code> otherwise
     */
    public abstract boolean isTrue();

    /**
     * The <code>Condition</code> object corresponding to an always satisfied condition that will return always <code>true</code>.
     */
    public static final Condition TRUE = new Condition() {
        public boolean isTrue() {
            return true;
        }
        
        @Override
        public String toString() {
            return "true";
        }
    };

    /**
     * The <code>Condition</code> object corresponding to an always unsatisfied condition that will return always <code>false</code>.
     */
    public static final Condition FALSE = new Condition() {
        public boolean isTrue() {
            return false;
        }

        @Override
        public String toString() {
            return "false";
        }
    };

    /** Create an OR condition that represent the logical operation <code>this || other </code>.
     *  @param other Condition to use for create the OR logical operation with this condition.
     *  @return new Condition representing the logical operation <code>this || other </code>.
     */
    public final Condition or(final Condition other) {
        return new Condition() {
            @Override
            public boolean isTrue() {
                return Condition.this.isTrue() || other.isTrue();
            }

            @Override
            public String toString() {
                return "(" + Condition.this.toString() + " || " + other.toString() + ")";
            }
        };
    }

    /** Create an XOR condition that represent the logical operation <code>this XOR other </code>.
     *  @param other Condition to use for create the XOR logical operation with this condition.
     *  @return new Condition representing the logical operation <code>this XOR other </code>.
     */
    public final Condition xor(final Condition other) {
        return new Condition() {
            @Override
            public boolean isTrue() {
                boolean a = Condition.this.isTrue();
                boolean b = other.isTrue();
                return (a && !b) || (b && !a);
            }

            @Override
            public String toString() {
                return "(" + Condition.this.toString() + " || " + other.toString() + ")";
            }
        };
    }

    /** Create an AND condition that represent the logical operation <code>this && other </code>.
     *  @param other Condition to use for create the AND logical operation with this condition.
     *  @return new Condition representing the logical operation <code>this && other </code>.
     */
    public final Condition and(final Condition other) {
        return new Condition() {
            @Override
            public boolean isTrue() {
                return Condition.this.isTrue() && other.isTrue();
            }

            @Override
            public String toString() {
                return "(" + Condition.this.toString() + " && " + other.toString() + ")";
            }
        };
    }

    /** Create a negate condition of this condition as <code>!this</code>.
     *  @return new Condition that is the negation of this condition
     */
    public final Condition not() {
        return new Condition() {
            @Override
            public boolean isTrue() {
                return !Condition.this.isTrue();
            }

            @Override
            public String toString() {
                return "!(" + Condition.this.toString() + ")";
            }
        };
    }
}