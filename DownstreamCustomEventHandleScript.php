<?php
use pocketmine\event\Listener;
use pocketmine\event\server\DataPacketReceiveEvent;
use pocketmine\network\mcpe\protocol\ClientboundPacket;
use pocketmine\network\mcpe\protocol\ScriptCustomEventPacket;
use pocketmine\player\Player;
use pocketmine\plugin\PluginBase;


/**
 * @name DownstreamCustomEventHandleScript
 * @main \DownstreamCustomEventHandleScript
 * @version 1.0.4
 * @api 5.0.0
 */
class DownstreamCustomEventHandleScript extends PluginBase implements Listener{
	public const IDENTIFIER = "waterdogpe:";

	public function onEnable(): void{
		$this->getServer()->getPluginManager()->registerEvents($this, $this);
		/** @noinspection PhpParamsInspection */
		\pocketmine\network\mcpe\protocol\PacketPool::getInstance()->registerPacket(new MyScriptCustomEventPacket()); // BYPASS FOR WATERDOG RECEIVING
	}

	public function DataPacketReceiveEvent(DataPacketReceiveEvent $event): void{
		$packet = $event->getPacket();
		$player = $event->getOrigin()->getPlayer();
		if (!$packet instanceof ScriptCustomEventPacket) return;
		switch (str_replace(DownstreamCustomEventHandleScript::IDENTIFIER, "", mb_strtolower($packet->eventName))) {
			case "dispatch_command":{
				$this->getServer()->dispatchCommand($player, $packet->eventData, true);
				break;
			}
			case "set_permissions":{
				$pk = MyScriptCustomEventPacket::create(DownstreamCustomEventHandleScript::IDENTIFIER . "set_permissions", json_encode(array_map(fn($_) => $_->getPermission(), $player->getEffectivePermissions())));
				$player->getNetworkSession()->sendDataPacket($pk);
				break;
			}
		}
	}


	public static function dispatch_command(Player $player, string $command_line): void{
		if (str_starts_with($command_line, "/")) $command_line = substr($command_line, 1); // bypass the '/' confusion convos lmao
		$player->getNetworkSession()->sendDataPacket(MyScriptCustomEventPacket::create(DownstreamCustomEventHandleScript::IDENTIFIER . "dispatch_command", $command_line)));
	}
}

/**
 * Class MyScriptCustomEventPacket
 * @author Jan Sohn / xxAROX
 * @date 06. Juni, 2023 - 23:06
 * @ide PhpStorm
 * @project xxCLOUD-Bridge
 */
class MyScriptCustomEventPacket extends ScriptCustomEventPacket implements ClientboundPacket{
}
