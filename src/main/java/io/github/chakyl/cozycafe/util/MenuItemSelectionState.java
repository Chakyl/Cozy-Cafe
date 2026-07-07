package io.github.chakyl.cozycafe.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MenuItemSelectionState {
    UNSET(0), INVALID(1), VALID(2);
    static final Map<Integer, MenuItemSelectionState> values = Arrays.stream(MenuItemSelectionState.values()).collect(Collectors.toMap(MenuItemSelectionState::getCode, Function.identity()));
    private final int code;

    MenuItemSelectionState(int code) {
        this.code = code;
    }

    public static MenuItemSelectionState fromCode(int code) {
        MenuItemSelectionState state = values.get(code);
        if (state == null) {
            return MenuItemSelectionState.UNSET;
        }
        return state;
    }

    public int getCode() {
        return this.code;
    }
}
