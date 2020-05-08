package fr.lesprojetscagnottes.core.stack;

import org.springframework.stereotype.Component;

import java.util.EmptyStackException;
import java.util.Stack;

@Component
public class DonationStack {

    Stack<DonationOperation> st = new Stack();

    static void showpush(Stack<DonationOperation> st, DonationOperation a) {
        st.push(a);
        System.out.println("push(" + a + ")");
        System.out.println("stack: " + st);
    }

    static void showpop(Stack<DonationOperation> st) {
        System.out.print("pop -> ");
        DonationOperation a = st.pop();
        System.out.println(a);
        System.out.println("stack: " + st);
    }

    public static void main(String args[]) {
        Stack<DonationOperation> st = new Stack<>();
        System.out.println("stack: " + st);
        showpush(st, 42);
        showpush(st, 66);
        showpush(st, 99);
        showpop(st);
        showpop(st);
        showpop(st);
        try {
            showpop(st);
        } catch (EmptyStackException e) {
            System.out.println("empty stack");
        }
    }

}
