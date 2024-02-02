package org.codegenerator.resourcescodegeneratorbuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SendingMoneyTransfer {
    private final User from;
    private final User to;
    private final int amount;

    private SendingMoneyTransfer(
            User from,
            User to,
            int amount
    ) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public int getAmount() {
        return amount;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User from;
        private User to;
        private int amount;

        private Builder() {
        }

        public User getFrom() {
            return from;
        }

        public User getTo() {
            return to;
        }

        public int getAmount() {
            return amount;
        }

        public Builder setFrom(User from) {
            this.from = from;
            return this;
        }

        public Builder setTo(User to) {
            this.to = to;
            return this;
        }

        public Builder setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public SendingMoneyTransfer build() {
            return new SendingMoneyTransfer(from, to, amount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendingMoneyTransfer that = (SendingMoneyTransfer) o;
        return amount == that.amount && Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, amount);
    }
}
