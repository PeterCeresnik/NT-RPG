/*    
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *     
 */

package cz.neumimto.commands;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public abstract class CommandBase implements CommandCallable {

    protected String permission = "*";
    protected Optional<Text> shortDescription = Optional.empty();
    protected Optional<Text> help = Optional.empty();
    protected Text usage = Texts.of("");
    protected List<String> alias = new ArrayList();

    public List<String> getAliases() {
        return alias;
    }

    @Override
    public CommandResult process(CommandSource commandSource, String s) throws CommandException {
        return CommandResult.empty();
    }

    protected void setDescription(String string) {
        /* Ty for epic wrapers use... */
        shortDescription = Optional.of(Texts.of(string));
    }

    protected void setHelp(String string) {
        help = Optional.of(Texts.of(string));
    }

    protected void setUsage(String string) {
        usage = Texts.of(string);
    }

    protected void addAlias(String string) {
        alias.add(string);
    }

    protected void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource commandSource) {
        if (permission.equalsIgnoreCase("*"))
            return true;
        return commandSource.hasPermission(permission);
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource commandSource) {
        return shortDescription;
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource commandSource) {
        return help;
    }


    @Override
    public Text getUsage(CommandSource commandSource) {
        return usage;
    }


}
