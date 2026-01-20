package org.helha.aemthackatonbackend.application.utils;

public interface IEffectCommandHandler<I> {
    void handle(I input);
}
