package com.prikolz.justhelper.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.prikolz.justhelper.JustHelperClient;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class SimpleCommand {
    private final List<SimpleCommandArg> line = new ArrayList<>();
    private final LiteralArgumentBuilder<ClientSuggestionProvider> main;
    private Predicate<ClientSuggestionProvider> requires = null;
    private BiFunction<CommandContext<ClientSuggestionProvider>, Throwable, Integer> onRunError = null;

    public SimpleCommand(String name) {
        this.main = JustHelperCommands.literal(name);
        this.onRunError = (context, error) -> {
            JustHelperCommand.feedback("<red>При выполнении команды произошла ошибка: {0}\nПодробнее /justhelper logs", error.getMessage());
            JustHelperClient.LOGGER.printStackTrace(error);
            return 0;
        };
    }

    public SimpleCommand add(LiteralArgumentBuilder<ClientSuggestionProvider> arg) {
        line.add(new LiteralArg(arg));
        return this;
    }

    public SimpleCommand add(RequiredArgumentBuilder<ClientSuggestionProvider, ?> arg) {
        line.add(new Arg(arg));
        return this;
    }

    public SimpleCommand arg(String name, ArgumentType<?> arg) {
        line.add(new Arg(JustHelperCommands.argument(name, arg)));
        return this;
    }

    public SimpleCommand requires(Predicate<ClientSuggestionProvider> predicate) {
        this.requires = predicate;
        return this;
    }

    public SimpleCommand run(Command<ClientSuggestionProvider> run) {
        if (onRunError != null) {
            BiFunction<CommandContext<ClientSuggestionProvider>, Throwable, Integer> onError = this.onRunError;
            try {
                line.get(line.size() - 1).get().executes(context -> {
                    try {
                        return run.run(context);
                    } catch (Throwable t) {
                        return onError.apply(context, t);
                    }
                });
            } catch (Throwable t) {
                main.executes(context -> {
                    try {
                        return run.run(context);
                    } catch (Throwable throwable) {
                        return onError.apply(context, throwable);
                    }
                });
            }
            return this;
        }

        try {
            line.getLast().get().executes(run);
        } catch (Throwable t) {
            main.executes(run);
        }
        return this;
    }

    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        int i = line.size();
        boolean emptyChild = true;
        SimpleCommandArg child = new LiteralArg(main);

        while (i > 0) {
            i--;
            SimpleCommandArg arg = line.get(i);
            if (emptyChild) {
                emptyChild = false;
                child = arg;
                continue;
            }
            arg.get().then(child.get());
            child = arg;
        }

        if (main == child.get()) {
            return main;
        }

        if (requires == null) {
            return main.then(child.get());
        } else {
            return main.requires(requires).then(child.get());
        }
    }

    // Абстрактный класс SimpleCommandArg
    abstract static class SimpleCommandArg {
        public abstract ArgumentBuilder<ClientSuggestionProvider, ?> get();
    }

    // Реализация для LiteralArgumentBuilder
    static class LiteralArg extends SimpleCommandArg {
        private final LiteralArgumentBuilder<ClientSuggestionProvider> arg;

        public LiteralArg(LiteralArgumentBuilder<ClientSuggestionProvider> arg) {
            this.arg = arg;
        }

        @Override
        public ArgumentBuilder<ClientSuggestionProvider, ?> get() {
            return this.arg;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LiteralArg that = (LiteralArg) o;
            return arg.equals(that.arg);
        }

        @Override
        public int hashCode() {
            return arg.hashCode();
        }

        @Override
        public String toString() {
            return "LiteralArg{arg=" + arg + '}';
        }
    }

    // Реализация для RequiredArgumentBuilder
    static class Arg extends SimpleCommandArg {
        private final RequiredArgumentBuilder<ClientSuggestionProvider, ?> arg;

        public Arg(RequiredArgumentBuilder<ClientSuggestionProvider, ?> arg) {
            this.arg = arg;
        }

        @Override
        public ArgumentBuilder<ClientSuggestionProvider, ?> get() {
            return this.arg;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Arg that = (Arg) o;
            return arg.equals(that.arg);
        }

        @Override
        public int hashCode() {
            return arg.hashCode();
        }

        @Override
        public String toString() {
            return "Arg{arg=" + arg + '}';
        }
    }
}