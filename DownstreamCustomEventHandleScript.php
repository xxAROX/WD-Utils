<?php
use pocketmine\plugin\PluginBase;
use pocketmine\event\Listener;
use pocketmine\event\server\DataPacketReceiveEvent;
use pocketmine\player\Player;
use pocketmine\network\mcpe\protocol\ScriptCustomEventPacket;


/**
 * @name DownstreamCustomEventHandleScript
 * @main \DownstreamCustomEventHandleScript
 * @version 1.0.0
 * @api 4.0.0
 */

class DownstreamCustomEventHandleScript extends PluginBase implements Listener{
    public const IDENTIFIER = "waterdogpe:";

    public function onEnable(): void{
        $this->getServer()->getPluginManager()->registerEvents($this, $this);
    }

	public static function dispatch_command(Player $player, string $command_line): void{
	    if (str_starts_with($command_line, "/")) $command_line = substr($command_line, 1);
	    $player->getNetworkSession()->sendPacket(ScriptCustomEventPacket::create(self::IDENTIFIER . "dispatch_command", $command_line));
	}

	public function DataPacketReceiveEvent(DataPacketReceiveEvent $event): void{
	    $packet = $event->getPacket();
        $player = $event->getOrigin()->getPlayer();
        if (!$packet instanceof ScriptCustomEventPacket) return;
        switch (str_replace(self::IDENTIFIER, "", mb_strtolower($packet->eventName))) {
            case "dispatch_command": {
                $this->getServer()->dispatchCommand($player, $packet->eventData);
                break;
            }
        }
	}
}
