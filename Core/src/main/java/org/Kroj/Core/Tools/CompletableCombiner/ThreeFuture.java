package org.Kroj.Core.Tools.CompletableCombiner;

@FunctionalInterface
public interface ThreeFuture<A, B, C, R> {
    R apply(A a, B b, C c);
}
