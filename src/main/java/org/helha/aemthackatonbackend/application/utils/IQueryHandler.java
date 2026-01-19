package org.helha.aemthackatonbackend.application.utils;

public interface IQueryHandler<I, O> {
    O handle(I input);
}
